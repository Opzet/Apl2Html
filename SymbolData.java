/**
 * @author Martin Gabor
 * @date 03/05/2017
 */
public class SymbolData {
    private ProgramData.Clazz clazz;
    private ProgramData.DataType type;

    public SymbolData(ProgramData.Clazz clazz) {
        this.clazz = clazz;
    }

    public SymbolData(ProgramData.Clazz clazz, ProgramData.DataType type) {
        this.clazz = clazz;
        this.type = type;
    }

    public ProgramData.DataType getType() {
        return type;
    }
}
