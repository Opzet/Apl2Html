
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
        public final Map<String, Var> localVars;

        public Function(int lineNumber) {
            this.lineNumber = lineNumber;
            parameters = new HashMap<>();
            localVars = new HashMap<>();
        }
    }

    public static class Var {
        public final String type;

        public Var(String type) {
            this.type = type;
        }
    }

    public static class Parameter extends Var {
        public boolean ret;

        public Parameter(String type, boolean ret) {
            super(type);
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
