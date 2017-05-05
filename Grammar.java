import java.util.List;
import java.util.regex.Pattern;

/**
 * @author m4g4
 * @date 10/04/2017
 */
public class Grammar {

    public static class LineMeta {

        public final int indentSize;

        public String line;
        public int lineIndent;
        public int lineNumber;
        public List<String> groups;
        public Grammar.LinePattern matchedPattern;

        public LineMeta(int indentSize, String line, int lineIndent, int lineNumber, List<String> groups, LinePattern matchedPattern) {
            this.indentSize = indentSize;
            this.line = line;
            this.lineIndent = lineIndent;
            this.lineNumber = lineNumber;
            this.groups = groups;
            this.matchedPattern = matchedPattern;
        }
    }

    public interface LinePattern {
        Pattern getPattern();
    }


    public static final LinePattern[] NO_PATTERNS = {};

    public enum General implements LinePattern {
        COMMENT_BLOCK("^!.*$"),
        DATA_DEFINITION("^\\.data.*"),
        END_DATA_DEFINITION("^\\.enddata");

        private final Pattern pattern;

        General(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Pattern getPattern() {
            return pattern;
        }
    }

    public enum Application implements LinePattern {
        COLLAPSE_ONLY("(^Formats|^Window Defaults|^Design-time Settings)"),
        VARIABLES("^Variables"), // TODO not used yet
        INTERNAL_FUNCTIONS("^Internal Functions"),
        EXTERNAL_FUNCTIONS("^External Functions"),
        LIBRARY_NAME("^Library name: ([_\\w]+)"),
        FUNCTION_DEFINITION("^Function: ([_\\w]+)"),
        CLASS("(^Form Window:|^Functional Class:|^Dialog Box:) ([_\\w]+)");

        private final Pattern pattern;

        Application(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Pattern getPattern() {
            return pattern;
        }
    }

    public enum Class implements LinePattern {
        FUNCTION_DEFINITION("^Function: ([_\\w]+)"),
        VARIABLES("(^Window Variables|^Class Variables|^Instance Variables)"),
        PARAMETERS("^Window Parameters"),
        MESSAGE_ACTIONS("^Message Actions");

        private final Pattern pattern;

        Class(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Pattern getPattern() {
            return pattern;
        }
    }

    public enum FunctionDefinition implements LinePattern {
        LOCAL_VARIABLES("^Local variables"),
        FUNCTIONAL_VAR("^FunctionalVar: +([_\\w]+).*"),
        VARIABLE("^(Receive ){0,1}([_\\w/]+[ _\\w/]*) *: +([_\\w]+).*"),
        PARAMETERS("^Parameters"),
        ACTIONS("^Actions");

        private final Pattern pattern;

        FunctionDefinition(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Pattern getPattern() {
            return pattern;
        }
    }

}
