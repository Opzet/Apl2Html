/**
 * @author m4g4
 * @date 24/04/2017
 */
public interface CanProcessLine {

    void process(StateContext context, Grammar.LineMeta meta, State newState);

    void closeScope(StateContext context);

}
