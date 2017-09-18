import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author m4g4
 * @date 11/04/2017
 */
public class ProgramData {

    public interface DataType {
        String getName();
        DataType getParent();
    }

    public static class Clazz implements DataType {
        public final Clazz parent;
        public final String name;
        public final Map<String, Function> functions;
        public final Map<String, Var> vars;
        public final Map<String, Clazz> classes;

        public Clazz(String name, Clazz parent) {
            this.name = name;
            this.parent = parent;
            functions = new LinkedHashMap<>();
            vars = new LinkedHashMap<>();
            classes = new LinkedHashMap<>();
        }

        public Clazz addNewClass(String name) {
            return classes.computeIfAbsent(name, v -> new Clazz(name, this));
        }

        public Function addNewFunction(String name) {
            return functions.computeIfAbsent(name, v -> new Function(name, this));
        }

        public Function addNewFunction(String name, boolean messageAction) {
            return functions.computeIfAbsent(name, v -> new Function(name, this, messageAction));
        }

        public Var addNewVar(String name, String type) {
            return vars.computeIfAbsent(name, v -> new Var(name, this, type));
        }

        public Var addNewVar(String name, String type, boolean parameter, boolean ret) {
            return vars.computeIfAbsent(name, v -> new Var(name, this, type, parameter, ret));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DataType getParent() {
            return parent;
        }
    }

    public static class Module extends Clazz {
        public Module(String name, Clazz parent) {
            super(name, parent);
        }
    }

    public static class Function implements DataType {
        public final Clazz parent;
        public final String name;
        public final boolean messageAction;
        public final Map<String, Var> vars;

        public Function(String name, Clazz parent) {
            this(name, parent, false);
        }

        public Function(String name, Clazz parent, boolean messageAction) {
            this.parent = parent;
            this.name = name;
            this.messageAction = messageAction;
            vars = new LinkedHashMap<>();
        }

        public Var addNewVar(String name, String type, boolean parameter, boolean ret) {
            return vars.computeIfAbsent(name, v -> new Var(name, this, type, parameter, ret));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DataType getParent() {
            return parent;
        }
    }

    public static class Var implements DataType {
        public final DataType parent;
        public final String name;
        public final String type;
        public boolean ret;
        public boolean parameter;

        public Var(String name, DataType parent, String type) {
            this(name, parent, type, false, false);
        }

        public Var(String name, DataType parent, String type, boolean parameter, boolean ret) {
            this.parent = parent;
            this.name = name;
            this.type = type;
            this.ret = ret;
            this.parameter = parameter;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DataType getParent() {
            return parent;
        }
    }

    public final Map<String, Module> modules;
    //public final Map<String, Function> externalFunctions;

    public ProgramData() {
        modules = new LinkedHashMap<>();
        //externalFunctions = new HashMap<>();
    }

    public static List<String> createSymbolParentChildList(DataType type) {
        List<String> symbols = new ArrayList<>();
        for (ProgramData.DataType t = type; t != null; t = t.getParent()) {
            symbols.add(0, t.getName());
        }

        return symbols;
    }
}
