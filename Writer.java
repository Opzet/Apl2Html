import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.plugin.dom.exception.InvalidStateException;

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

        meta.line = Utils.htmlEscape(meta.line);

        if (Grammar.General.COMMENT_BLOCK.equals(meta.matchedPattern) ||
                State.COMMENT_BLOCK.equals(context.getCurrentState())) {
            converted.add(build(meta, "comment"));
            return;
        }

        replaceComments(meta);

        switch (context.getCurrentState()) {
        case INITIAL:
            converted.add(build(meta));
            context.setCurrentClass(data.modules.get(context.getModuleName()));
            break;

        case APPLICATION:
            if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                replaceClassDefinitionSymbols(context, data, meta);
                context.setCurrentClass(context.getCurrentClass().classes.get(meta.groups.get(2)));
            }

            converted.add(build(meta));
            break;

        case MESSAGE_ACTIONS:
            if (Grammar.Class.MESSAGE_DEFINITION.equals(meta.matchedPattern)) {
                replaceFunctionDefinitionSymbols(context, data, meta);
            }

            converted.add(build(meta));
            break;
        case CLASS:
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                replaceFunctionDefinitionSymbols(context, data, meta);
            } else if (Grammar.Class.VARIABLE_DEFINITION.equals(meta.matchedPattern)) {
                replaceVarDefinitions(context, data, meta);
                context.setCurrentClass(context.getCurrentClass().classes.get(meta.groups.get(2)));
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
        if (context.getCurrentState() == State.APPLICATION ||
                context.getCurrentState() == State.VARIABLE_DEFINITION ||
                context.getCurrentState() == State.CLASS)
            context.setCurrentClass(context.getCurrentClass().parent);
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

        return getTabEscaped(line, meta);
    }

    protected static String getTabEscaped(String line, Grammar.LineMeta meta) {
        return line.replace("\t", String.join("", Collections.nCopies(meta.indentSize, " ")));
    }

    private static void replaceClassDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {
        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);
        for (Utils.Token token : tokens) {

            ProgramData.Clazz clazz = c.getCurrentClass().classes.get(token.getToken());

            if (clazz != null) {
                List<String> symbols = ProgramData.createSymbolParentChildList(clazz);
                res = res.replaceFirst(token.getToken(), "<a id= '"+Utils.constructSymbolId(symbols)+"' class='class_def'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceExternalFunctionDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);

        ProgramData.Function line;
        for (Utils.Token token : tokens) {
            line = c.getCurrentClass().functions.get(token.getToken());

            if (line != null) {
                res = res.replaceFirst(token.getToken(), "<a href='' class='function_def external'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceFunctionDefinitionSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        String res = meta.line;

        List<Utils.Token> tokens = Utils.tokenize(meta.line);

        // TODO MAGA test if can merge with replaceExternalFunctionDefinitionSymbols

        ProgramData.Function function;
        for (Utils.Token token : tokens) {
            function = c.getCurrentClass().functions.get(token.getToken());

            if (function != null) {
                List<String> symbols = ProgramData.createSymbolParentChildList(function);
                res = res.replaceFirst(token.getToken(), "<a id='"+Utils.constructSymbolId(symbols)+"' class='function_def'>" + token.getToken() + "</a>");
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
            if (c.getFnName() == null) {
                var = c.getCurrentClass().vars.get(token.getToken());
                classVar = true;
            } else {
                var = c.getCurrentClass().functions.get(c.getFnName()).vars.get(token.getToken());
            }

            if (var != null) {
                String classParam = classVar ? "cl" : "";
                String paramVar = var.parameter ? "parameter_def" : "var_def";

                List<String> symbols = ProgramData.createSymbolParentChildList(var);

                res = res.replaceAll(token.getToken(), "<a id='" + Utils.constructSymbolId(symbols) +
                        "' class='"+paramVar+" "+classParam+"'>" + token.getToken() + "</a>");
            }
        }

        meta.line = res;
    }

    private static void replaceSymbols(StateContext c, ProgramData data, Grammar.LineMeta meta) {

        ProgramData.Clazz currentClazz = c.getCurrentClass();
        ProgramData.Function currentFunction;
        List<SymbolData> symbolChain = new ArrayList<>();

        List<Utils.Token> tokens = Utils.tokenize(meta.line);
        for (int i = 0; i < tokens.size(); ++i) {
            Utils.Token token = tokens.get(i);

            if (token.getOffset() > 1 &&
                    token.getLine().substring(token.getOffset() - 2, token.getOffset()).equals("..")) {

                /*// It's an inheritance
                if (symbolChain.isEmpty())
                    scope.addAll(resolveInheritanceChain(c.getCurrentClass()));

                else {
                    SymbolData contextSymbol = findContextSymbol(token, tokens, symbolChain);
                    if (contextSymbol == null || !(contextSymbol.getType() instanceof ProgramData.Var)) {
                        symbolChain.clear();
                        continue;
                    }

                    if (contextSymbol.getType())
                }*/

                currentFunction = null;

            } else if (token.getOffset() > 0 &&
                    token.getLine().substring(token.getOffset() - 1, token.getOffset()).equals(".")) {
                // It's a class method call or class variable access

                if (symbolChain.isEmpty())
                    continue;

                SymbolData contextSymbol = findContextSymbol(token, tokens, symbolChain);

                if (contextSymbol == null || !(contextSymbol.getType() instanceof ProgramData.Var)) {
                    symbolChain.clear();
                    continue;
                }

                ProgramData.Var var = (ProgramData.Var) contextSymbol.getType();

                currentClazz = findClass(currentClazz, data, var.type);
                currentFunction = null;

            } else {
                currentClazz = c.getCurrentClass();
                currentFunction = c.getCurrentClass().functions.get(c.getFnName());
                symbolChain.clear();
            }

            if (currentClazz == null) {
                // TODO log me
                continue;
            }

            SymbolData symbolData = null;
            if (currentFunction != null)
                symbolData = findVariable(currentFunction, data, token.getToken());

            if (symbolData == null)
                symbolData = findVariable(currentClazz, data, token.getToken());

            if (symbolData == null)
                symbolData = findFunction(currentClazz, data, token.getToken());

            if (symbolData == null)
                continue; // not found

            symbolChain.add(symbolData);

            ProgramData.Function fn = c.getCurrentClass().functions.get(c.getFnName());
            Set<ProgramData.DataType> usages = data.symbolUsages.computeIfAbsent(symbolData, e -> new HashSet<>());
            usages.add(fn);
            replaceToken(symbolData, tokens, i, meta);
        }
    }

    private static SymbolData findContextSymbol(Utils.Token token, List<Utils.Token> tokens, List<SymbolData> symbolChain) {

        String line = token.getLine();

        if (line.charAt(token.getOffset() - 1) != '.') {
            throw new InvalidStateException(". expected at index " + (token.getOffset() - 1) + " in line " + line);
        }

        int startIndex = line.charAt(token.getOffset() - 2) == '.' ? token.getOffset() - 3 : token.getOffset() - 2;

        Stack<Character> parenthesis = new Stack<>();
        for (int i = startIndex; i > 0; --i) {
            char c = token.getLine().charAt(i);

            if (parenthesis.isEmpty()) {
                if (c == ']' || c == ')') {
                    parenthesis.push(c);

                } else {
                    for (int j = tokens.size() - 1; j >= 0; --j) {
                        Utils.Token tkn = tokens.get(j);

                        if (tkn.getOffset() <= i) {
                            Optional<SymbolData> symbol = symbolChain.stream()
                                    .filter(s -> s.getType().getName().equals(tkn.getToken()))
                                    .findFirst();
                            return symbol.isPresent() ? symbol.get() : null;
                        }
                    }
                }

            } else if (c == '[' || c == '(') {
                parenthesis.pop();
            }
        }

        return null;
    }

    private static void replaceToken(SymbolData symbolData, List<Utils.Token> tokens, int tokenNumber, Grammar.LineMeta meta) {
        StringBuilder clazz = new StringBuilder();
        String id = null;
        if ((symbolData.getType() instanceof ProgramData.Var)) {
            ProgramData.Var v = (ProgramData.Var) symbolData.getType();
            clazz.append(v.parameter ? "parameter " : "var ");
            clazz.append(v.ret ? "ret " : "");
            clazz.append(v.parent instanceof ProgramData.Function ? "" : "cl ");

        } else if ((symbolData.getType() instanceof ProgramData.Function)) {
            clazz.append("function ");
        }

        id = Utils.constructRefId(ProgramData.createSymbolParentChildList(symbolData.getType()));

        meta.line = Utils.replaceToken(meta.line, tokens, tokenNumber, "<a href='" + id + "' class='"+clazz+"'>" + tokens.get(tokenNumber).getToken() + "</a>");

    }

    private static SymbolData findVariable(ProgramData.Clazz clazz, ProgramData data, String symbol) {

        // class vars context
        ProgramData.Var var = clazz.vars.get(symbol);
        if (var != null)
            return new SymbolData(var);

        if (clazz.anonymousInstance)
            return findVariable((ProgramData.Clazz) clazz.getParent(), data, symbol);

        return null;
    }

    private static SymbolData findVariable(ProgramData.Function function, ProgramData data, String symbol) {

        ProgramData.Var var = function.vars.get(symbol);
        if (var != null)
            return new SymbolData(var);

        if (((ProgramData.Clazz) function.getParent()).anonymousInstance) {
            SymbolData symbolData = findVariable((ProgramData.Clazz) function.getParent(), data, symbol);
            if (symbolData != null)
                return symbolData;
        }

        for (Map.Entry<String, ProgramData.Module> pair: data.modules.entrySet()) {
            var = pair.getValue().vars.get(symbol);
            if (var != null)
                return new SymbolData(var);
        }

        return null;
    }

    private static SymbolData findFunction(ProgramData.Clazz clazz, ProgramData data, String symbol) {

        ProgramData.Function fn = clazz.functions.get(symbol);
        if (fn != null)
            return new SymbolData(fn);

        if (clazz.anonymousInstance) {
            SymbolData symbolData = findFunction((ProgramData.Clazz) clazz.getParent(), data, symbol);
            if (symbolData != null)
                return symbolData;
        }

        for (Map.Entry<String, ProgramData.Module> pair: data.modules.entrySet()) {
            fn = pair.getValue().functions.get(symbol);
            if (fn != null)
                return new SymbolData(fn);
        }

        return null;
    }

    private static ProgramData.Clazz findClass(ProgramData.Clazz c, ProgramData data, String symbol) {

        ProgramData.Clazz node = c;
        while (node != null) {
            ProgramData.Clazz cl = node.classes.get(symbol);

            if (cl != null) {
                return cl;
            }
            else
                node = node.parent;
        }

        ProgramData.Clazz cl;

        /*
        * TODO: The following searching should be replaced with searching in included libraries list only.
        * */
        for (Map.Entry<String, ProgramData.Module> pair: data.modules.entrySet()) {
            cl = pair.getValue().classes.get(symbol);
            if (cl != null)
                return cl;
        }

        return null;
    }
}
