
import java.util.HashMap;
import java.util.Map;

/**
 * @author m4g4
 * @date 11/04/2017
 */
public class ProgramData {

    public static class Module {
        public final Map<String, Function> internalFunctions;
        public final Map<String, Clazz> classes;

        public Module() {
            internalFunctions = new HashMap<>();
            classes = new HashMap<>();
        }
    }

    public static class Clazz {
        public final Map<String, Function> functions;
        public final Map<String, Var> vars;

        public Clazz() {
            functions = new HashMap<>();
            vars = new HashMap<>();
        }
    }

    public static class Function {
        public final int lineNumber;
        public final Map<String, Var> vars;

        public Function(int lineNumber) {
            this.lineNumber = lineNumber;
            vars = new HashMap<>();
        }
    }

    public static class Var {
        public final String type;
        public boolean ret;
        public boolean parameter;

        public Var(String type) {
            this(type, false, false);
        }

        public Var(String type, boolean parameter, boolean ret) {
            this.type = type;
            this.ret = ret;
            this.parameter = parameter;
        }
    }

    public final Map<String, Module> modules;
    public final Map<String, Function> externalFunctions;

    public ProgramData() {
        modules = new HashMap<>();
        externalFunctions = new HashMap<>();
    }
}
