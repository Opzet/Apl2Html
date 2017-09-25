import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Apl2html {

    public static void main(String[] args) {

        Arguments arguments = Arguments.processArgs(args);
        if (arguments == null) {
            usage();
            return;
        }

        Parser parser = parse(arguments);

        if (parser != null) {
            printSymbolUsages(parser, arguments.getOutputDir());
        }
    }

    private static void printSymbolUsages(Parser parser, String path) {
        System.out.print("Creating symbol usages file _symbols.json ... ");

        String outjs = path + File.separator + "_symbols.json";
        Path parentDir = Paths.get(outjs).getParent();
        try {
            if (!Files.exists(parentDir))
                Files.createDirectories(parentDir);

            Files.write(Paths.get(outjs), parser.createSymbolUsages());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("done!");
    }

    private static void usage() {
        System.out.println("Usage apl2txt directory [-o output dir] [-s spaces per indent] [-i ignored nodes path]");
    }

    private static Parser parse(Arguments arguments) {
        if(!checkPath(arguments.getInputDir())) {
            System.out.println("Input directory not found!");
            return null;
        }

        Set<Grammar.LinePattern> ignoredNodes = arguments.getIgnoredNodesPath() != null ?
                readIgnoredNodes(arguments.getIgnoredNodesPath()) : Collections.emptySet();

        if (ignoredNodes == null)
            return null;

        Parser parser = new Parser(arguments.getIndent(), ignoredNodes);

        List<Module> modules = getAplModules(arguments.getInputDir());

        // Read modules first
        modules.forEach(module -> {
            System.out.print("Reading module " + module.getModuleName() +" ... ");
            parser.read(module);
            System.out.println("done!");
        });

        System.out.println("------------------------------");

        // Write modules to html
        modules.forEach(module -> {

            System.out.print("Writing module " + module.getModuleName() + " ... ");

            String out = arguments.getOutputDir() + File.separator + module.getModuleName() + ".html";
            Path parentDir = Paths.get(out).getParent();
            try {
                if (!Files.exists(parentDir))
                    Files.createDirectories(parentDir);

                Files.write(Paths.get(out), parser.convert(module));
            } catch (IOException e) {
                System.out.println("");
                System.out.println("failed! File: " + out);
                return;
            }

            System.out.println("done! File: " + out);
        });

        System.out.println("Finished");
        System.out.println("------------------------------");

        return parser;
    }

    private static Set<Grammar.LinePattern> readIgnoredNodes(String file) {
        List<String> lines = new ArrayList<>();

        String line;
        try (
                InputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)
        ) {
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.append("Failed reading ignored nodes file: " + file);
            return null;
        }

        List<Pattern> ignoredPatterns;
        try {
            ignoredPatterns = Utils.createPatterns(lines);
        } catch (PatternSyntaxException e) {
            System.out.print("Invalid pattern " + e.getPattern() + " .");
            return null;
        }

        Set<Grammar.LinePattern> result = ignoredPatterns.stream()
                .map(e -> (Grammar.LinePattern) () -> e)
                .collect(Collectors.toSet());

        return result;
    }

    private static boolean checkPath(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    private static List<Module> getAplModules(String path) {

        List<Module> aplModules = new ArrayList<>();

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(path))) {
                paths.forEach(filePath -> {
                    if (Files.isRegularFile(filePath) && Module.isFileAplModule(filePath)) {
                        aplModules.add(new Module(filePath));
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace(); // TODO fix me
        }

        return aplModules;
    }
}
