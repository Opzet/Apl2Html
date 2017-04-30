
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
        public final Map<String, Integer> vars;
        public final Map<String, Parameter> parameters;

        public Clazz() {
            functions = new HashMap<>();
            vars = new HashMap<>();
            parameters = new HashMap<>();
        }
    }

    public static class Function {
        public final int lineNumber;
        public final Map<String, Parameter> parameters;
        public final Map<String, Integer> localVars;

        public Function(int lineNumber) {
            this.lineNumber = lineNumber;
            parameters = new HashMap<>();
            localVars = new HashMap<>();
        }
    }

    public static class Parameter {
        public final int lineNumber;
        public boolean ret;

        public Parameter(int lineNumber, boolean ret) {
            this.lineNumber = lineNumber;
            this.ret = ret;
        }
    }

    public final Map<String, Module> modules;
    public final Map<String, Function> externalFunctions;

    public ProgramData() {
        modules = new HashMap<>();
        externalFunctions = new HashMap<>();
    }
}
