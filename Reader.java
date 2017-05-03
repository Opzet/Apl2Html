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
            data.modules.put(context.getModuleName(), new ProgramData.Module());
            break;

        case APPLICATION:
            if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                data.modules.get(context.getModuleName())
                        .classes.put(meta.groups.get(2), new ProgramData.Clazz());

            }
            break;

        case INTERNAL_FUNCTIONS:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                data.modules.get(context.getModuleName())
                        .internalFunctions.put(meta.groups.get(1), new ProgramData.Function(meta.lineNumber));
                break;
            }

        case EXTERNAL_LIBRARY:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                data.externalFunctions.put(meta.groups.get(1), new ProgramData.Function(meta.lineNumber));
                break;
            }

        case CLASS:
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                data.modules.get(context.getModuleName()).classes.get(context.getClName())
                        .functions.put(meta.groups.get(1), new ProgramData.Function(meta.lineNumber));
            }
            break;

        case VARIABLES:
            if (Grammar.FunctionDefinition.VARIABLE.equals(meta.matchedPattern)) {

                String type;
                String name;
                if (context.getCustomData() != null) {
                    type = meta.groups.get(3);
                    name = (String) context.getCustomData();
                    context.setCustomData(null); // TODO terrible hack
                } else {
                    type = meta.groups.get(2);
                    name = meta.groups.get(3);
                }

                if (context.getClName() == null) {
                    if (context.getFnName() != null) {
                        if (context.getExternalLibName() != null) {
                            data.externalFunctions.get(context.getFnName()).localVars.put(name, new ProgramData.Var(type));
                        } else {
                            data.modules.get(context.getModuleName()).internalFunctions.get(context.getFnName())
                                    .localVars.put(name, new ProgramData.Var(type));
                        }
                    }
                } else {
                    if (context.getFnName() == null) {
                        data.modules.get(context.getModuleName()).classes.get(context.getClName())
                                .vars.put(name, new ProgramData.Var(type));
                    } else {
                        data.modules.get(context.getModuleName()).classes.get(context.getClName()).functions.get(context.getFnName())
                                .localVars.put(name, new ProgramData.Var(type));
                    }
                }
            }
            break;

        case PARAMETERS:
            if (Grammar.FunctionDefinition.VARIABLE.equals(meta.matchedPattern)) {

                String type;
                String name;
                if (context.getCustomData() != null) {
                    type = meta.groups.get(3);
                    name = (String) context.getCustomData();
                    context.setCustomData(null); // TODO terrible hack
                } else {
                    type = meta.groups.get(2);
                    name = meta.groups.get(3);
                }

                if (context.getClName() == null) {
                    if (context.getFnName() != null) {
                        if (context.getExternalLibName() != null) {
                            data.externalFunctions.get(context.getFnName()).localVars.put(name, new ProgramData.Var(type));
                        } else {
                            data.modules.get(context.getModuleName()).internalFunctions.get(context.getFnName()).
                                    parameters.put(name, new ProgramData.Parameter(type, meta.groups.get(1) != null));
                        }
                    }
                } else {
                    if (context.getFnName() == null) {
                        data.modules.get(context.getModuleName()).classes.get(context.getClName())
                                .parameters.put(name, new ProgramData.Parameter(type, meta.groups.get(1) != null));
                    } else {
                        data.modules.get(context.getModuleName()).classes.get(context.getClName()).functions.get(context.getFnName())
                                .parameters.put(name, new ProgramData.Parameter(type, meta.groups.get(1) != null));
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
