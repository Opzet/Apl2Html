import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                replaceClassDefinitionSymbols(context, data, meta);
            }

            converted.add(build(meta));
            break;

        case CLASS:
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                replaceFunctionDefinitionSymbols(context, data, meta);
            }

            converted.add(build(meta));
            break;

        case INTERNAL_FUNCTIONS:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                replaceFunctionDefinitionSymbols(context, data, meta);
                converted.add(build(meta));
            }
            break;

        case EXTERNAL_FUNCTIONS:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                replaceExternalFunctionDefinitionSymbols(context, data, meta);
                converted.add(build(meta));
            }
            break;

        case VARIABLES:
            replaceVarDefinitions(context, data, meta);
            converted.add(build(meta));
            break;

        case PARAMETERS:
            replaceVarDefinitions(context, data, meta);
            converted.add(build(meta));
            break;

        case FUNCTION_BODY:
            replaceSymbols(context, data, meta);

            converted.add(build(meta));
            break;

        default:
            converted.add(build(meta));
        }

    }

    @Override
    public void closeScope(StateContext context) {
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

    private static void replaceClassDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {
        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);
        for (Utils.Token token : tokens) {

            ProgramData.Clazz clazz = data.modules.get(c.getModuleName()).classes.get(token.getToken());

            if (clazz != null) {
                res = res.replaceFirst(token.getToken(), "<a id= '"+Utils.constructSymbolId(token.getToken())+"' class='class_def'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceExternalFunctionDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);

        ProgramData.Function line;
        for (Utils.Token token : tokens) {
            line = data.externalFunctions.get(token.getToken());

            if (line != null) {
                res = res.replaceFirst(token.getToken(), "<a href='' class='function_def external'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceFunctionDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);

        ProgramData.Function line;
        for (Utils.Token token : tokens) {
            if (c.getClName() == null) {
                line = data.modules.get(c.getModuleName()).internalFunctions.get(token.getToken());
            } else {
                line = data.modules.get(c.getModuleName()).classes.get(c.getClName())
                        .functions.get(token.getToken());
            }

            if (line != null) {
                res = res.replaceFirst(token.getToken(), "<a id='"+Utils.constructSymbolId(c.getClName(), token.getToken())+"' class='function_def'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceVarDefinitions(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);
        for (Utils.Token token : tokens) {
            ProgramData.Var var;

            boolean classVar = false;
            if (c.getClName() == null) {
                var = data.modules.get(c.getModuleName()).internalFunctions.get(c.getFnName())
                        .vars.get(token.getToken());
            } else {
                if (c.getFnName() == null) {
                    var = data.modules.get(c.getModuleName()).classes.get(c.getClName())
                            .vars.get(token.getToken());
                    classVar = true;
                } else {
                    var = data.modules.get(c.getModuleName()).classes.get(c.getClName()).functions.get(c.getFnName())
                            .vars.get(token.getToken());
                }
            }

            if (var != null) {
                String classParam = classVar ? "cl" : "";
                String paramVar = var.parameter ? "parameter_def" : "var_def";

                res = res.replaceAll(token.getToken(), "<a id='" +
                        Utils.constructSymbolId(c.getClName(), c.getFnName(), token.getToken()) +
                        "' class='"+paramVar+" "+classParam+"'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        List<Utils.Token> tokens = Utils.tokenize(meta.line);
        for (int i = 0; i < tokens.size(); ++i) {
            Utils.Token token = tokens.get(i);

            SymbolData symbolData = null;
            StateContext symbolContext = c;
            if (!token.getRightChar().equals('.')) {
                symbolData = findVariable(symbolContext, data, token.getToken());
                if (symbolData == null)
                    symbolData = findFunction(symbolContext, data, token.getToken());

                if (symbolData !=null) {
                    replaceToken(symbolData, tokens, i, meta);
                }

                continue;

            } else {
                symbolData = findVariable(symbolContext, data, token.getToken());
                if (symbolData == null)
                    continue; // not found
                else {
                    replaceToken(symbolData, tokens, i, meta);
                }
            }

            ProgramData.Var var = (ProgramData.Var) symbolData.getSymbol();
            SymbolData classData = findClass(symbolContext, data, var.type);

            if (classData == null)
                continue;

            symbolContext = new StateContext();
            symbolContext.setModuleName(classData.getModuleName());
            symbolContext.setClName(classData.getClassName());
        }
    }

    private static void replaceToken(SymbolData symbolData, List<Utils.Token> tokens, int tokenNumber, Grammar.LineMeta meta) {
        StringBuilder clazz = new StringBuilder();
        String id = null;
        if ((symbolData.getSymbol() instanceof ProgramData.Var)) {
            ProgramData.Var v = (ProgramData.Var) symbolData.getSymbol();
            clazz.append(v.parameter ? "parameter " : "var ");
            clazz.append(v.ret ? "ret " : "");

            id = Utils.constructRefId(symbolData.getModuleName(), symbolData.getClassName(), symbolData.getFunctionName(), tokens.get(tokenNumber).getToken());
        } else if ((symbolData.getSymbol() instanceof ProgramData.Function)) {
            clazz.append("function ");
            id = Utils.constructRefId(symbolData.getModuleName(), symbolData.getClassName(), null, tokens.get(tokenNumber).getToken());
        }

        clazz.append(symbolData.getFunctionName() == null ? "cl " : "");

        meta.line = Utils.replaceToken(meta.line, tokens, tokenNumber, "<a href='" + id + "' class='"+clazz+"'>" + tokens.get(tokenNumber).getToken() + "</a>");

    }

    private static SymbolData findVariable(StateContext c, ProgramData data, String symbol) {

        ProgramData.Var var = null;
        if (c.getClName() != null) {

            if (c.getFnName() != null) { // class function context
                var = data.modules.get(c.getModuleName()).classes.get(c.getClName()).functions.get(c.getFnName()).vars.get(symbol);
                if (var != null)
                    return new SymbolData(c.getModuleName(), c.getClName(), c.getFnName(), symbol, var);
            }

            // class vars context
            var = data.modules.get(c.getModuleName()).classes.get(c.getClName()).vars.get(symbol);
            if (var != null)
                return new SymbolData(c.getModuleName(), c.getClName(), null, symbol, var);

        } else {

            if (c.getFnName() != null) { // internal function context
                var = data.modules.get(c.getModuleName()).internalFunctions.get(c.getFnName()).vars.get(symbol);
                if (var != null)
                    return new SymbolData(c.getModuleName(), null, c.getFnName(), symbol, var);
            }
        }

        return null;
    }

    private static SymbolData findFunction(StateContext c, ProgramData data, String symbol) {

        ProgramData.Function fn = null;
        if (c.getClName() != null) {

            // class function context
            fn = data.modules.get(c.getModuleName()).classes.get(c.getClName()).functions.get(symbol);
            if (fn != null)
                return new SymbolData(c.getModuleName(), c.getClName(), symbol, null, fn);

        } else {
            fn = data.modules.get(c.getModuleName()).internalFunctions.get(symbol);
            if (fn != null)
                return new SymbolData(c.getModuleName(), null, symbol, null, fn);
        }

        fn = data.externalFunctions.get(symbol);
        if (fn != null)
            return new SymbolData(null, c.getClName(), symbol, null, fn);

        return null;
    }

    private static SymbolData findClass(StateContext c, ProgramData data, String symbol) {

        ProgramData.Clazz cl = data.modules.get(c.getModuleName()).classes.get(symbol);
        if (cl != null)
            return new SymbolData(c.getModuleName(), symbol, null, null, cl);

        /*
        * TODO: The following searching should be replaced with searching in included libraries list only.
        * */
        for (Map.Entry<String, ProgramData.Module> pair: data.modules.entrySet()) {
            cl = pair.getValue().classes.get(symbol);
            if (cl != null)
                return new SymbolData(pair.getKey(), symbol, null, null, cl);
        }

        return null;
    }
}
