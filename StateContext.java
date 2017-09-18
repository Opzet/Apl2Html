/**
 * @author m4g4
 * @date 24/04/2017
 */
public class StateContext {
    private String moduleName;
    private String externalLibName;
    private String fnName;
    private Object customData;

    private ProgramData.Clazz currentClass;
    private State currentState;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getExternalLibName() {
        return externalLibName;
    }

    public void setExternalLibName(String externalLibName) {
        this.externalLibName = externalLibName;
    }

    public String getClazzName() {
        return currentClass.name;
    }

    public String getFnName() {
        return fnName;
    }

    public void setFnName(String fnName) {
        this.fnName = fnName;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public Object getCustomData() {
        return customData;
    }

    public void setCustomData(Object customData) {
        this.customData = customData;
    }

    public ProgramData.Clazz getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(ProgramData.Clazz currentClass) {
        this.currentClass = currentClass;
    }
}
