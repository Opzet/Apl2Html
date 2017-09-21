import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author m4g4
 * @date 05/04/2017
 */
public class Parser {
    private static Pattern linePattern = Pattern.compile("\\.head ([\\d]+) [+|-] *");

    private final int indentSize;
    private final ProgramData programData;

    public Parser(int indentSize) {
        this.indentSize = indentSize;

        programData = new ProgramData();
    }

    public ProgramData getProgramData() {
        return programData;
    }

    public void read(Module module) {

        State.INITIAL.setAppName(module.getModuleName());
        traverse(new Reader(programData), module, State.INITIAL);
    }

    public List<String> convert(Module module) {

        Writer writer = new Writer(programData);

        addHeader(writer);
        startDataSection(writer);

        writer.getConverted().add("<h1>" + module.getModuleName() + "</h1>");
        State.INITIAL.setAppName(module.getModuleName());

        traverse(writer, module, State.INITIAL);

        finishDataSection(writer);

        addNavigation(module, writer);

        addFooter(writer);

        return writer.getConverted();
    }

    public List<String> createSymbolUsages() {

        List<String> result = new ArrayList<>();
        result.add("var symbols = {");

        boolean firstSymbol = true;
        for (Map.Entry<SymbolData, Set<ProgramData.DataType>> symbol : programData.symbolUsages.entrySet()) {

            String symbolLink = Utils.constructSymbolId(ProgramData.createSymbolParentChildList(symbol.getKey().getType()));

            if (!firstSymbol)
                result.add(",");
            else
                firstSymbol = false;

            result.add("\"" + symbolLink + "\": [");

            boolean firstSource = true;
            for (ProgramData.DataType dataType : symbol.getValue()) {
                if (!firstSource)
                    result.add(",");
                else
                    firstSource = false;

                String sourceSymbolLink = Utils.constructRefId(ProgramData.createSymbolParentChildList(dataType));
                result.add("\"" + sourceSymbolLink + "\"");
            }

            result.add("]");
        }

        result.add("}");
        return result;
    }

    private void traverse(CanProcessLine processor, Module module, State initState) {

        Stack<State> states = new Stack<>();
        states.push(initState);
        initState.setIndent(-1);

        StateContext context = new StateContext();
        context.setCurrentState(initState);

        /* Traverse lines */
        int currentIndent, newIndent = 0, lineNumber = 0;
        boolean ignoreSection = false;

        for (int i = 0; i < module.getLines().size(); ++i) {
            String line = module.getLines().get(i);
            ++lineNumber;

            currentIndent = newIndent;

            String[] heading = getHeading(line);
            if(heading != null) {
                line = line.substring(heading[0].length(), line.length());
                newIndent = Integer.parseInt(heading[1]);
            } else {
                newIndent = currentIndent;
            }

            if (!ignoreSection) {
                ignoreSection = isDataSection(line, false);
                if (ignoreSection){
                    continue;
                }
            } else {
                ignoreSection = isDataSection(line, true);
                continue;
            }

            while (newIndent <= states.peek().getIndent()) {
                states.pop().closeScope(context);
                processor.closeScope(context);
                context.setCurrentState(states.peek());
            }

            State currentState = states.peek();

            Grammar.LineMeta meta = parseLine(currentState.getGrammar(), line, newIndent, lineNumber);

            State newState = currentState.process(context, meta);

            processor.process(context, meta);

            if (currentState != newState) {
                context.setCurrentState(newState);
                newState.setIndent(newIndent);
                states.push(newState);
            }
        }
    }

    private Grammar.LineMeta parseLine(Grammar.LinePattern[] patterns, String lineContent, int lineIndent, int lineNumber) {

        List<Grammar.LinePattern> patternList = new ArrayList<>(patterns.length * 2);
        patternList.addAll(Arrays.asList(patterns));
        patternList.addAll(Arrays.asList(Grammar.General.values()));

        for (Grammar.LinePattern pattern : patternList) {
            Matcher matcher = pattern.getPattern().matcher(lineContent);
            if(matcher.find()) {

                List<String> groups = new ArrayList<>(matcher.groupCount() + 1);

                for (int i = 0; i <= matcher.groupCount(); ++i) {
                    groups.add(matcher.group(i));
                }

                return new Grammar.LineMeta(indentSize, lineContent, lineIndent, lineNumber, groups, pattern);
            }
        }

        return new Grammar.LineMeta(indentSize, lineContent, lineIndent, lineNumber, null,null);
    }

    private boolean isDataSection(String line, boolean ignoreSection) {
        Grammar.LineMeta meta = parseLine(Grammar.NO_PATTERNS, line, 0, 0);

        if (ignoreSection) {
            if (Grammar.General.END_DATA_DEFINITION.equals(meta.matchedPattern)) {
                return false;
            }
        } else {
            if (Grammar.General.DATA_DEFINITION.equals(meta.matchedPattern)) {
                return true;
            }
        }

        return ignoreSection;
    }

    private void addHeader(Writer writer) {
        writer.getConverted().add("<html>" +
                "<head>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=windows-1250\">" +
                "<script\n" +
                "  src=\"https://code.jquery.com/jquery-2.2.4.min.js\"\n" +
                "  integrity=\"sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44=\"\n" +
                "  crossorigin=\"anonymous\"></script>" +
                "<script type=\"text/javascript\" language=\"javascript\" src='ui/scripts.js'></script>" +
                "<script type=\"text/javascript\" language=\"javascript\" src=\"_symbols.json\"></script> \n"+

                // BOOTSTRAP
                "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" " +
                        "integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">" +
                "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" " +
                        "integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>" +

                // STYLES
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"ui/style.css\">" +
                "</head>" +
                "<body>");
    }

    private void addFooter(Writer writer) {
        writer.getConverted().add("</body></html>");
    }

    private void startDataSection(Writer writer) {
        writer.getConverted().add("<div class='data'>");
    }

    private void finishDataSection(Writer writer) {
        writer.getConverted().add("</div>");
    }

    private void addNavigation(Module module, Writer writer) {
        writer.getConverted().add("<nav>");

        writer.getConverted().add("<ul>");
        programData.modules.get(module.getModuleName()).classes.entrySet().forEach(clazz -> {
            printClazz(writer, clazz.getValue());
        });

        writer.getConverted().add("</ul>");
        writer.getConverted().add("</nav>");
    }

    private static void printClazz(Writer writer, ProgramData.Clazz clazz) {

        String classRefId = Utils.constructRefId(ProgramData.createSymbolParentChildList(clazz));
        String classId = Utils.constructSymbolId(ProgramData.createSymbolParentChildList(clazz));

        if (clazz.classes.isEmpty() && clazz.functions.isEmpty() && clazz.vars.isEmpty()) {
            writer.getConverted().add("<li><a href="+classRefId+">" + clazz.getName() + "</a></li>");
            return;
        }

        writer.getConverted().add("<li>" + Utils.constructCollapser(classId, "<a href="+classRefId+">" + clazz.getName() + "</a></li>"));

        writer.getConverted().add("<ul>");

        if (!clazz.classes.isEmpty()) {
            writer.getConverted().add("<li>Classes:</li>");
            clazz.classes.entrySet().forEach(subClazz -> printClazz(writer, subClazz.getValue()));
        }

        if (!clazz.functions.isEmpty()) {
            writer.getConverted().add("<li>Functions:</li>");
            clazz.functions.entrySet().forEach(function -> {
                String functionId = Utils.constructRefId(ProgramData.createSymbolParentChildList(function.getValue()));
                writer.getConverted().add("<li><a href=" + functionId + ">" + function.getKey() + "</a></li>");
            });
        }

        if (!clazz.vars.isEmpty()) {
            writer.getConverted().add("<li>Variables:</li>");
            clazz.vars.entrySet().forEach(var -> {
                if (!clazz.classes.containsKey(var.getValue().getName())) {
                    String varId = Utils.constructRefId(ProgramData.createSymbolParentChildList(var.getValue()));
                    writer.getConverted().add("<li><a href=" + varId + ">" + var.getKey() + "</a></li>");
                }
            });
        }

        writer.getConverted().add("</ul>");
        writer.getConverted().add("</div>"); // close collapser
    }

    private static String[] getHeading(String line) {
        Matcher matcher = linePattern.matcher(line);
        if(matcher.find()) {
            String str = matcher.group(1); // index 0 is the whole found string
            if (str == null || "".equals(str))
                return null;

            return new String[]{matcher.group(0), matcher.group(1)};
        }
        return null;
    }
}
