/**
 * @author m4g4
 * @date 13/04/2017
 */
public class Reader implements CanProcessLine {

    private final ProgramData data;

    public Reader(ProgramData data) {
        this.data = data;
    }

    @Override
    public void process(StateContext context, Grammar.LineMeta meta) {

        boolean parameter = false;
        switch (context.getCurrentState()) {
        case INITIAL:
            ProgramData.Module m = new ProgramData.Module(context.getModuleName(), null);
            data.modules.put(context.getModuleName(), m);
            context.setCurrentClass(m);
            break;

        case APPLICATION:
            if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                ProgramData.Clazz c = data.modules.get(context.getModuleName()).addNewClass(meta.groups.get(2), false);
                context.setCurrentClass(c);
            }
            break;

        case INTERNAL_FUNCTIONS:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                data.modules.get(context.getModuleName()).addNewFunction(meta.groups.get(1));
                break;
            }

        case EXTERNAL_LIBRARY:
            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                //data.modules.get(context.getModuleName()).addNewFunction(meta.groups.get(1)); // TODO is this ??
                break;
            }

        case MESSAGE_ACTIONS:
            if (Grammar.Class.MESSAGE_DEFINITION.equals(meta.matchedPattern)) {
                context.getCurrentClass().addNewFunction(meta.groups.get(1), true);
            }
            break;
        case CLASS:
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                context.getCurrentClass().addNewFunction(meta.groups.get(1));
            } else if (Grammar.Class.VARIABLE_DEFINITION.equals(meta.matchedPattern)) {
                ProgramData.Clazz clazz = context.getCurrentClass().addNewClass(meta.groups.get(2), true);
                context.getCurrentClass().addNewVar(meta.groups.get(2), meta.groups.get(1));
                context.setCurrentClass(clazz);
            }
            break;

        case CLASS_INHERITANCE_DEFINITION:
            if (Grammar.ClassInheritanceDefinition.CLASS.equals(meta.matchedPattern)) {
                context.getCurrentClass().setBaseClassType(meta.groups.get(1));
            }
            break;

        case PARAMETERS:
            parameter = true;
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

                if (context.getFnName() == null) {
                    context.getCurrentClass().addNewVar(name, type, parameter, meta.groups.get(1) != null);
                } else {
                    context.getCurrentClass().functions.get(context.getFnName())
                            .addNewVar(name, type, parameter, meta.groups.get(1) != null);
                }
            }
            break;
        }
    }

    @Override
    public void closeScope(StateContext context) {
        if (context.getCurrentState() == State.APPLICATION ||
                context.getCurrentState() == State.VARIABLE_DEFINITION ||
                context.getCurrentState() == State.CLASS)
        context.setCurrentClass(context.getCurrentClass().parent);
    }
}
