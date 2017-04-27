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
        public List<String> tokens;
        public Grammar.LinePattern matchedPattern;

        public LineMeta(int indentSize, String line, int lineIndent, int lineNumber, List<String> groups, List<String> tokens, LinePattern matchedPattern) {
            this.indentSize = indentSize;
            this.line = line;
            this.lineIndent = lineIndent;
            this.lineNumber = lineNumber;
            this.groups = groups;
            this.tokens = tokens;
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
        FUNCTION_DEFINITION("^Function: ([_A-Za-z\\d]+)"),
        CLASS("(^Form Window:|^Functional Class:|^Dialog Box:) ([_A-Za-z\\d]+)");

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
        FUNCTION_DEFINITION("^Function: ([_A-Za-z\\d]+)"),
        VARIABLES("^Window Variables"),
        PARAMETERS("^Window Parameters");

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
        VARIABLE("^(Receive )*([_A-Za-z\\d/]+[ _A-Za-z\\d/]*) *: +([_A-Za-z\\d]+).*"),
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
