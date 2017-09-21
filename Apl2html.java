import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Apl2html {

    public static int DEFAULT_INDENTS = 4;

    public static void main(String[] args) {
	    if(args.length < 1) {
	        usage();
	        return;
        }

        Parser parser;
        if(args.length == 2) {
	        int indents;
            try {
                indents = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect indents input, default will be used ");
                indents = DEFAULT_INDENTS;
            }
            parser = parse(args[0], indents);
        } else
            parser = parse(args[0], DEFAULT_INDENTS);

        if (parser != null) {
            printSymbolUsagesHtml(parser, args[0]);
        }
    }

    private static void printSymbolUsagesHtml(Parser parser, String path) {
        System.out.print("Creating symbol usages file into _symbols.json ... ");

        String outjs = path + "/output/_symbols.json";
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
        System.out.println("Usage apl2txt directory [spaces per indent - default "+DEFAULT_INDENTS+"]");
    }

    private static Parser parse(String path, int indents) {
        if(!checkPath(path)) {
            System.out.println("Input directory not found!");
            return null;
        }

        Parser parser = new Parser(indents);

        List<Module> modules = getAplModules(path);

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

            String out = path + "/output/" + module.getModuleName() + ".html"; // TODO output folder should be input parameter
            Path parentDir = Paths.get(out).getParent();
            try {
                if (!Files.exists(parentDir))
                    Files.createDirectories(parentDir);

                Files.write(Paths.get(out), parser.convert(module));
            } catch (IOException e) {
                e.printStackTrace(); // TODO fix me
            }

            System.out.println("done! File: " + out);
        });

        System.out.println("Finished");
        System.out.println("------------------------------");

        return parser;
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
