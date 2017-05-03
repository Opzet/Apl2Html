import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author m4g4
 * @date 21/04/2017
 */
public class Module {

    private final static String APL_EXT = "apl";

    private final String moduleName;
    private final List<String> lines;

    public Module(Path path) {
        moduleName = Arrays.asList(path.getFileName().toString().split("\\.")).get(0); // TODO fixme
        lines = getLines(path);
    }

    private static List<String> getLines(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }

        return null; // TODO
    }

    public String getModuleName() {
        return moduleName;
    }

    public List<String> getLines() {
        return lines;
    }

    public static boolean isFileAplModule(Path path) {
        List<String> pathTokens = Arrays.asList(path.toAbsolutePath().toString().split("\\.")); // TODO fixme

        return pathTokens.size() == 2 && pathTokens.get(1).equalsIgnoreCase(APL_EXT);
    }
}
