/**
 * @author m4g4
 * @date 13/04/2017
 */
public class Reader implements CanProcessLine {

    private final ProgramData data;

    Reader(ProgramData data) {
        this.data = data;
    }

    @Override
    public void process(StateContext context, Grammar.LineMeta meta) {
        switch (context.getCurrentState()) {
        case INITIAL:
            data.apps.put(context.getModuleName(), new ProgramData.Application());
            break;

        case APPLICATION:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                data.apps.get(context.getModuleName())
                        .globalFunctions.put(meta.groups.get(1), new ProgramData.Function(meta.lineNumber));

            } else if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                data.apps.get(context.getModuleName())
                        .classes.put(meta.groups.get(2), new ProgramData.Clazz());

            }
            break;

        case CLASS:
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                data.apps.get(context.getModuleName()).classes.get(context.getClName())
                        .functions.put(meta.groups.get(1), new ProgramData.Function(meta.lineNumber));
            }
            break;

        case VARIABLES:
            if (Grammar.FunctionDefinition.VARIABLE.equals(meta.matchedPattern)) {
                if (context.getClName() == null) {
                    if (context.getFnName() == null) {
                        // TODO
                    } else {
                        data.apps.get(context.getModuleName()).globalFunctions.get(context.getFnName())
                                .localVars.put(meta.groups.get(3), meta.lineNumber);
                    }
                } else {
                    if (context.getFnName() == null) {
                        data.apps.get(context.getModuleName()).classes.get(context.getClName())
                                .vars.put(meta.groups.get(3), meta.lineNumber);
                    } else {
                        data.apps.get(context.getModuleName()).classes.get(context.getClName()).functions.get(context.getFnName()).localVars.put(meta.groups.get(3), meta.lineNumber);
                    }
                }
            }
            break;

        case PARAMETERS:
            if (Grammar.FunctionDefinition.VARIABLE.equals(meta.matchedPattern)) {
                if (context.getClName() == null) {
                    if (context.getFnName() == null) {
                        // TODO
                    } else {
                        data.apps.get(context.getModuleName()).globalFunctions.get(context.getFnName()).
                                parameters.put(meta.groups.get(3),
                                new ProgramData.Parameter(meta.lineNumber, meta.groups.get(1) != null));
                    }
                } else {
                    if (context.getFnName() == null) {
                        data.apps.get(context.getModuleName()).classes.get(context.getClName())
                                .parameters.put(meta.groups.get(3),
                                new ProgramData.Parameter(meta.lineNumber, meta.groups.get(1) != null));
                    } else {
                        data.apps.get(context.getModuleName()).classes.get(context.getClName()).functions.get(context.getFnName())
                                .parameters.put(meta.groups.get(3),
                                new ProgramData.Parameter(meta.lineNumber, meta.groups.get(1) != null));
                    }
                }
            }
            break;
        }
    }

    @Override
    public void closeScope(StateContext context) {
    }
}
