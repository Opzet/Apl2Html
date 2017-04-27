import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author m4g4
 * @date 13/04/2017
 */
public class Writer implements CanProcessLine {

    private final ProgramData data;
    private final List<String> converted;
    
    Writer(ProgramData data) {
        this.data = data;
        this.converted = new ArrayList<>();
    }
    
    @Override
    public void process(StateContext context, Grammar.LineMeta meta) {

        if (Grammar.General.COMMENT_BLOCK.equals(meta.matchedPattern) ||
                State.COMMENT_BLOCK.equals(context.getCurrentState())) {
            converted.add(build(meta, "comment"));
            return;
        }

        replaceComments(meta);

        switch (context.getCurrentState()) {
        case INITIAL:
            converted.add(build(meta));
            break;

        case APPLICATION:
            if (Grammar.Application.COLLAPSE_ONLY.equals(meta.matchedPattern)) {
                converted.add(constructCollapser(context, meta.groups.get(1), build(meta)));

            }  else if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                replaceClassDefinitionSymbols(context, data, meta);
                converted.add(constructCollapser(context, meta.groups.get(2), build(meta)));
                
            } else if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                replaceFunctionDefinitionSymbols(context, data, meta);
                converted.add(constructCollapser(context, meta.groups.get(1), build(meta)));

            } else {
                converted.add(build(meta));
            }
            break;

        case CLASS:
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                replaceFunctionDefinitionSymbols(context, data, meta);
                converted.add(constructCollapser(context, meta.groups.get(1), build(meta)));

            } else if (Grammar.Class.VARIABLES.equals(meta.matchedPattern)) {
                converted.add(build(meta));

            } else if (Grammar.Class.PARAMETERS.equals(meta.matchedPattern)) {
                converted.add(build(meta));

            } else {
                converted.add(build(meta));
            }
            break;

        case VARIABLES:
            replaceVarDefinitions(context, data, meta);
            converted.add(build(meta));
            break;

        case PARAMETERS:
            replaceParameterDefinitions(context, data, meta);
            converted.add(build(meta));
            break;

        case FUNCTION_BODY:
            replaceVarsSymbols(context, data, meta);
            replaceParametersSymbols(context, data, meta);

            converted.add(build(meta));
            break;

        default:
            converted.add(build(meta));
        }

    }

    @Override
    public void closeScope(StateContext context) {
        switch (context.getCurrentState()) {
        case COLLAPSED:
        case CLASS:
        case FUNCTION_DEFINITION:
            converted.add("</div>"); // end of collapsed
        }
    }

    public List<String> getConverted() {
        return converted;
    }

    protected void replaceComments(Grammar.LineMeta meta) {
        String regex = "[ \t]![ \t]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(meta.line);
        if (m.find()) {
            meta.line = meta.line.replaceFirst(regex, " <span class='comment'>! ") + "</span>";
        }
    }

    protected boolean replaceConstants(Grammar.LineMeta meta) {

        // TODO
        // replace number constants
        //for (String token : meta.tokens) {

            //if (NumberUtils.isNumber())
            //    meta.line = Utils.replaceToken(meta.line, token, "<span class='const'>" + token + "</span>");
        //}

        return false;
    }

    private static final String build(Grammar.LineMeta meta) {
        return build(meta, null);
    }

    private static String build(Grammar.LineMeta meta, String classNames) {

        String line = "";
        if (meta.lineIndent > 0)
            line = String.join("", Collections.nCopies(meta.lineIndent * meta.indentSize, " "));

        if (classNames == null) {
            line = line + meta.line;
        } else {
            line = line + "<span class=\"" + classNames + "\">" + meta.line + "</span>";
        }

        return getEscaped(line, meta);
    }

    protected static String getEscaped(String line, Grammar.LineMeta meta) {
        return line.replace("\t", String.join("", Collections.nCopies(meta.indentSize, " ")));
    }

    private static String constructCollapser(StateContext context, String id, String line) {
        String fullId = "collapse_" + Utils.constructSymbolId(context.getClName(), context.getFnName(), id);
        return line + "<i class='glyphicon glyphicon-plus' data-toggle='collapse' href='#" + fullId + "'></i>" +
                "<div class='collapse' id='" + fullId + "'>";
    }

    private static void replaceClassDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {
        String res = meta.line;

        for (String token : meta.tokens) {

            ProgramData.Clazz clazz = data.apps.get(c.getModuleName()).classes.get(token);

            if (clazz != null) {
                res = res.replaceFirst(token, "<a href='' class='class_def'>" + token + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceFunctionDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        ProgramData.Function line;
        for (String token : meta.tokens) {
            if (c.getClName() == null) {
                line = data.apps.get(c.getModuleName()).globalFunctions.get(token);
            } else {
                line = data.apps.get(c.getModuleName()).classes.get(c.getClName())
                        .functions.get(token);
            }

            if (line != null) {
                res = res.replaceFirst(token, "<a href='' class='function_def'>" + token + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceVarDefinitions(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        for (String token : meta.tokens) {
            Integer line;

            boolean classVar = false;
            if (c.getClName() == null) {
                // TODO if (c.getFnName() == null)
                line = data.apps.get(c.getModuleName()).globalFunctions.get(c.getFnName()).localVars.get(token);
            } else {
                if (c.getFnName() == null) {
                    line = data.apps.get(c.getModuleName()).classes.get(c.getClName())
                            .vars.get(token);
                    classVar = true;
                } else {
                    line = data.apps.get(c.getModuleName()).classes.get(c.getClName()).functions.get(c.getFnName())
                            .localVars.get(token);
                }
            }

            if (line != null) {
                String classParam = classVar ? "cl" : "";

                res = res.replaceFirst(token, "<a id='" +
                        Utils.constructSymbolId(c.getClName(), c.getFnName(), token) +
                        "' class='var_def "+classParam+"'>" + token + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceParameterDefinitions(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        for (String token : meta.tokens) {
            ProgramData.Parameter parameter;

            boolean classVar = false;
            if (c.getClName() == null) {
                // TODO if (c.getFnName() == null)
                parameter = data.apps.get(c.getModuleName()).globalFunctions.get(c.getFnName())
                        .parameters.get(token);
            } else {
                if (c.getFnName() == null) {
                    parameter = data.apps.get(c.getModuleName()).classes.get(c.getClName())
                            .parameters.get(token);
                    classVar = true;
                } else {
                    parameter = data.apps.get(c.getModuleName()).classes.get(c.getClName()).functions.get(c.getFnName())
                            .parameters.get(token);
                }
            }

            if (parameter != null) {
                String classParam = classVar ? "cl" : "";

                res = res.replaceAll(token, "<a id='" +
                        Utils.constructSymbolId(c.getClName(), c.getFnName(), token) +
                        "' class='parameter_def "+classParam+"'>" + token + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceVarsSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        Set<String> replacedTokens = new TreeSet<>();

        for (String token : meta.tokens) {
            if (replacedTokens.contains(token))
                continue;

            Integer line = null;
            boolean classVar = false;
            StateContext symbolContext = new StateContext();
            symbolContext.setModuleName(c.getModuleName());

            if (c.getClName() == null) {
                // TODO if (c.getFnName() == null)
                line = data.apps.get(c.getModuleName()).globalFunctions.get(c.getFnName()).localVars.get(token);
                symbolContext.setFnName(c.getFnName());
            } else {
                symbolContext.setClName(c.getClName());
                if (c.getFnName() != null) {
                    line = data.apps.get(c.getModuleName()).classes.get(c.getClName()).functions.get(c.getFnName())
                            .localVars.get(token);
                }
                if (line == null) {
                    line = data.apps.get(c.getModuleName()).classes.get(c.getClName())
                            .vars.get(token);
                    classVar = true;
                } else
                    symbolContext.setFnName(c.getFnName());
            }

            if (line != null) {
                String classParam = classVar ? "cl" : "";

                res = Utils.replaceToken(res, token, "<a href='" +
                                Utils.constructRefId(symbolContext.getModuleName(), symbolContext.getClName(), symbolContext.getFnName(), token) +
                                "' class='var " +classParam+"'>" + token + "</a>");

                replacedTokens.add(token);
            }
        }

        meta.line = res;
    }

    private static void replaceParametersSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        Set<String> replacedTokens = new TreeSet<>();

        for (int i = 0; i < meta.tokens.size(); ++i) {
            String token = meta.tokens.get(i);
            if (replacedTokens.contains(token))
                continue;

            ProgramData.Parameter parameter = null;
            boolean classVar = false;
            StateContext symbolContext = new StateContext();
            symbolContext.setModuleName(c.getModuleName());
            if (c.getClName() == null) {
                // TODO if (c.getFnName() == null)
                parameter = data.apps.get(c.getModuleName()).globalFunctions.get(c.getFnName()).parameters.get(token);
                symbolContext.setFnName(c.getFnName());
            } else {
                symbolContext.setClName(c.getClName());
                if (c.getFnName() != null) {
                    parameter = data.apps.get(c.getModuleName()).classes.get(c.getClName()).functions.get(c.getFnName())
                            .parameters.get(token);
                }
                if (parameter == null) {
                    parameter = data.apps.get(c.getModuleName()).classes.get(c.getClName())
                            .parameters.get(token);
                    classVar = true;
                } else
                    symbolContext.setFnName(c.getFnName());
            }

            if (parameter != null) {
                String returnParam = parameter.ret ? "ret" : "";
                String classParam = classVar ? "cl" : "";

                res = Utils.replaceToken(res, token, "<a href='" +
                        Utils.constructRefId(symbolContext.getModuleName(), symbolContext.getClName(), symbolContext.getFnName(), token) +
                        "' class='parameter "+ returnParam +" "+classParam+"'>" + token + "</a>");

                replacedTokens.add(token);
            }
        }

        meta.line = res;
    }
}
