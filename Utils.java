import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author m4g4
 * @date 21/04/2017
 */
public class Utils {
    public static final String DELIMITER = " .,;'=+-*/()[]<>:\t";

    public static String constructSymbolId(List<String> symbols) {
        StringBuilder refId = new StringBuilder();

        for (int i = 1; i < symbols.size(); i++) {
            refId.append(normalizeSymbol(symbols.get(i)));

            if (i + 1 < symbols.size())
                refId.append("_");
        }

        return refId.toString();
    }

    public static String constructRefId(List<String> symbols) {
        return normalizeSymbol(symbols.get(0)) + ".html#" + constructSymbolId(symbols);
    }

    private static String normalizeSymbol(String symbol) {
        return symbol.replaceAll(" ", "_");
    }

    public static String replaceToken(String line, List<Utils.Token> tokens, int tokenNumber, String value) {

        String tmp = line;

        // iterate over all tokens and replace only one.
        int i = -1;
        for (int it = 0; it < tokens.size(); ++it) {

            String token = tokens.get(it).getToken();

            for (; (i = tmp.indexOf(token, i)) != -1; ++i) {
                if (i != 0) {
                    char ch = tmp.charAt(i - 1);
                    if (DELIMITER.indexOf(ch) == -1) {
                        i += token.length();
                        continue;
                    }
                }

                if (i + token.length() != tmp.length()) {
                    char ch = tmp.charAt(i + token.length());
                    if (DELIMITER.indexOf(ch) == -1) {
                        i += token.length();
                        continue;
                    }
                }

                // replace only this token
                if (it == tokenNumber) {
                    return tmp.substring(0, i) + value + tmp.substring(i + token.length(), tmp.length());
                } else {
                    i += token.length();
                    break;
                }
            }
        }

        return tmp;
    }

    public static String htmlEscape(String line) {
        return line.replace("\"", "&quot;").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static class Token {
        private int offset;
        private String line;
        private String token;

        public Token(int offset, String line, String token) {
            this.offset = offset;
            this.line = line;
            this.token = token;
        }

        public int getOffset() {
            return offset;
        }

        public String getToken() {
            return token;
        }

        public String getLine() {
            return line;
        }

        @Override
        public String toString() {
            return token;
        }
    }

    public static List<Token> tokenize(String line) {
        List<Token> tokens = new ArrayList<>();

        StringBuilder lastBuilder = null;
        int pos = 0;

        for (int i = 0; i < line.length(); ++i ) {
            char ch = line.charAt(i);
            if (DELIMITER.indexOf(ch) == -1) {
                if (lastBuilder == null) {
                    pos = i;
                    lastBuilder = new StringBuilder();
                }
                lastBuilder.append(ch);
            } else {
                if (lastBuilder != null) {
                    tokens.add(new Token(pos, line, lastBuilder.toString()));
                    lastBuilder = null;
                }
            }
        }

        if (lastBuilder != null) {
            tokens.add(new Token(pos, line, lastBuilder.toString()));
        }

        return tokens;
    }

    public static List<String> tokenize(String line, String delimiter) {

        List<String> tokens = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(line, delimiter);

        while (st.hasMoreElements()) {
            tokens.add((String) st.nextElement());
        }

        return tokens;
    }

    public static String constructCollapser(String id, String line) {
        String fullId = "collapse_" + id;
        return "<i class='glyphicon glyphicon-plus' data-toggle='collapse' href='#" + fullId + "'></i>" + line +
                "<div class='collapse' id='" + fullId + "'>";
    }
}
