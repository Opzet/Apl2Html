import java.util.Objects;

/**
 * @author Martin Gabor
 * @date 03/05/2017
 */
public class SymbolData {
    private ProgramData.DataType type;

    public SymbolData(ProgramData.DataType type) {
        this.type = type;
    }

    public ProgramData.DataType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolData that = (SymbolData) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
