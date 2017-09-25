import java.io.File;

/**
 * @author Martin Gabor
 * @date 22/09/2017
 */

public class Arguments {

    public static int DEFAULT_INDENTS = 4;
    public static final String DEFAULT_OUTPUT_DIR = "output";

    private final String inputDir;
    private final int indent;
    private String ignoredNodesPath;
    private String outputDir;

    public Arguments(String inputDir, int indent) {
        this(inputDir, indent, null, null);
    }

    public Arguments(String inputDir, int indent, String ignoredNodesPath, String outputDir) {
        this.inputDir = inputDir;
        this.indent = indent;
        this.ignoredNodesPath = ignoredNodesPath;
        this.outputDir = outputDir;
    }

    public String getInputDir() {
        return inputDir;
    }

    public int getIndent() {
        return indent;
    }

    public String getIgnoredNodesPath() {
        return ignoredNodesPath;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public static Arguments processArgs(String[] args) {

        if (args.length < 1)
            return null;

        String inputDir = args[0];

        if (args.length == 1) {
            return new Arguments(inputDir, DEFAULT_INDENTS);
        }

        String outputDir = inputDir + File.separator + DEFAULT_OUTPUT_DIR;
        Integer indents = DEFAULT_INDENTS;
        String ignoredNodesPath = null;

        // args length > 1
        for (int i = 1; i < args.length; i++) {

            // each arg has identifier
            if (i + 1 >= args.length)
                return null;

            if ("-s".equals(args[i])) {
                indents = parseIndents(args[i+1]);
                ++i;
                if (indents == null)
                    return null;

            } else if ("-i".equals(args[i])) {
                ignoredNodesPath = args[i+1];
                ++i;

            } else if ("-o".equals(args[i])) {
                outputDir = args[i+1];
                ++i;
            }
        }

        return new Arguments(inputDir, indents, ignoredNodesPath, outputDir);
    }

    private static Integer parseIndents(String indentString) {
        try {
            return Integer.parseInt(indentString);
        } catch (NumberFormatException e) {
            System.out.println("Incorrect indents input.");
        }

        return null;
    }
}
