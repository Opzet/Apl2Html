/**
 * @author Martin Gabor
 * @date 03/05/2017
 */
public class SymbolData {
    private String moduleName;
    private String className;
    private String functionName;
    private String varName;
    private Object symbol;

    public SymbolData(String moduleName, String className, Object symbol) {
        this.moduleName = moduleName;
        this.className = className;
        this.symbol = symbol;
    }

    public SymbolData(String moduleName, String className, String functionName, Object symbol) {
        this.moduleName = moduleName;
        this.className = className;
        this.functionName = functionName;
        this.symbol = symbol;
    }

    public SymbolData(String moduleName, String className, String functionName, String varName, Object symbol) {
        this.moduleName = moduleName;
        this.className = className;
        this.functionName = functionName;
        this.varName = varName;
        this.symbol = symbol;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getClassName() {
        return className;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getVarName() {
        return varName;
    }

    public Object getSymbol() {
        return symbol;
    }
}
