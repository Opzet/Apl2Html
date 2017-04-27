/**
 * @author m4g4
 * @date 21/04/2017
 */
public class Utils {
    public static final String DELIMITER = " .,;=()+-*/:\t";

    public static String constructSymbolId(String clazz, String function, String symbol) {
        return (clazz != null ? clazz + "_" : "") +
                (function != null ? function + "_" : "") +
                normalizeSymbol(symbol);
    }

    public static String constructRefId(String module, String clazz, String function, String symbol) {
        return module + ".html#" +
                (clazz != null ? clazz + "_" : "") +
                (function != null ? function + "_" : "") +
                normalizeSymbol(symbol);
    }

    private static String normalizeSymbol(String symbol) {
        return symbol.replaceAll(" ", "_");
    }

    public static String replaceToken(String line, String token, String value) {

        String tmp = line;

        for (int i = -1; (i = tmp.indexOf(token, i)) != -1; ) {
            if (i != 0) {
                if (Character.isLetter(tmp.charAt(i - 1)) || Character.isDigit(tmp.charAt(i - 1))) {
                    i += token.length();
                    continue;
                }
            }

            if (i + token.length() != tmp.length()) {
                if (Character.isLetter(tmp.charAt(i + token.length())) || Character.isDigit(tmp.charAt(i + token.length()))) {
                    i += token.length();
                    continue;
                }
            }

            tmp = tmp.substring(0,i) + value + tmp.substring(i+token.length(),tmp.length());

            i += value.length();
        }

        return tmp;
    }
}
