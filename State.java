/**
 * @author m4g4
 * @date 24/04/2017
 */
public enum State {
    INITIAL(Grammar.NO_PATTERNS) {
        @Override
        public State getNewState(StateContext context, Grammar.LineMeta meta) {
            context.setModuleName(getAppName());
            return APPLICATION;
        }

        private String appName;

        @Override
        public String getAppName() {
            if (appName == null)
                throw new RuntimeException("App name not set!");
            return appName;
        }

        @Override
        public void setAppName(String appName) {
            this.appName = appName;
        }
    },
    APPLICATION(Grammar.Application.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta) {

            if (Grammar.Application.INTERNAL_FUNCTIONS.equals(meta.matchedPattern)) {
                return INTERNAL_FUNCTIONS;

            } else if (Grammar.Application.EXTERNAL_FUNCTIONS.equals(meta.matchedPattern)) {
                return EXTERNAL_FUNCTIONS;

            } else if (Grammar.Application.CLASS.equals(meta.matchedPattern)) {
                return CLASS;

            } else if (Grammar.Application.COLLAPSE_ONLY.equals(meta.matchedPattern)) {
                return COLLAPSED;
            }

            return APPLICATION;
        }

        @Override
        public void closeScope(StateContext context) {
            context.setModuleName(null);
        }
    },

    INTERNAL_FUNCTIONS(Grammar.Application.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta) {

            if (Grammar.Application.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                context.setFnName(meta.groups.get(1));
                return FUNCTION_DEFINITION;
            }

            return INTERNAL_FUNCTIONS;
        }
    },

    EXTERNAL_FUNCTIONS(Grammar.Application.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta) {

            if (Grammar.Application.LIBRARY_NAME.equals(meta.matchedPattern)) {
                context.setExternalLibName(meta.groups.get(1));
                return EXTERNAL_LIBRARY;
            }

            return EXTERNAL_FUNCTIONS;
        }
    },

    EXTERNAL_LIBRARY(Grammar.Application.values()) {
        @Override
        public void closeScope(StateContext context) {
            context.setExternalLibName(null);
        }
    },

    CLASS(Grammar.Class.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta) {
            if (Grammar.Class.FUNCTION_DEFINITION.equals(meta.matchedPattern)) {
                context.setFnName(meta.groups.get(1));
                return FUNCTION_DEFINITION;

            } else if (Grammar.Class.VARIABLES.equals(meta.matchedPattern)) {
                return VARIABLES;
            } else if (Grammar.Class.PARAMETERS.equals(meta.matchedPattern)) {
                return PARAMETERS;
            } else if (Grammar.Class.MESSAGE_ACTIONS.equals(meta.matchedPattern)) {
                return MESSAGE_ACTIONS;
            }  else if (Grammar.Class.VARIABLE_DEFINITION.equals(meta.matchedPattern)) {
                return VARIABLE_DEFINITION;
            }

            return CLASS;
        }
    },

    VARIABLE_DEFINITION(Grammar.Class.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta) {
            if (Grammar.Class.MESSAGE_ACTIONS.equals(meta.matchedPattern)) {
                return MESSAGE_ACTIONS;
            }

            return VARIABLE_DEFINITION;
        }
    },

    MESSAGE_ACTIONS(Grammar.Class.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta) {
            if (Grammar.Class.MESSAGE_DEFINITION.equals(meta.matchedPattern)) {
                return FUNCTION_BODY;
            }

            return MESSAGE_ACTIONS;
        }
    },

    FUNCTION_DEFINITION(Grammar.FunctionDefinition.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta){

            if (Grammar.FunctionDefinition.ACTIONS.equals(meta.matchedPattern)) {
                return FUNCTION_BODY;
            } else if (Grammar.FunctionDefinition.LOCAL_VARIABLES.equals(meta.matchedPattern)) {
                return VARIABLES;
            } else if (Grammar.FunctionDefinition.PARAMETERS.equals(meta.matchedPattern)) {
                return PARAMETERS;
            } else
                return FUNCTION_DEFINITION;
        }

        @Override
        public void closeScope(StateContext context) {
            context.setFnName(null);
        }
    },

    VARIABLES(Grammar.FunctionDefinition.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta){

            if (Grammar.FunctionDefinition.FUNCTIONAL_VAR.equals(meta.matchedPattern)) {
                context.setCustomData(meta.groups.get(1));
            }

            return VARIABLES;
        }

        @Override
        public void closeScope(StateContext context) {
            context.setCustomData(null);
        }
    },

    PARAMETERS(Grammar.FunctionDefinition.values()) {
        @Override
        public State getNewState (StateContext context, Grammar.LineMeta meta){

            if (Grammar.FunctionDefinition.FUNCTIONAL_VAR.equals(meta.matchedPattern)) {
                context.setCustomData(meta.groups.get(1));
            }

            return PARAMETERS;
        }

        @Override
        public void closeScope(StateContext context) {
            context.setCustomData(null);
        }
    },

    FUNCTION_BODY(Grammar.FunctionDefinition.values()),

    COMMENT_BLOCK(Grammar.NO_PATTERNS),

    COLLAPSED(Grammar.NO_PATTERNS);

    private Grammar.LinePattern[] grammar;
    private int indent;

    State(Grammar.LinePattern[] grammar) {
        this.grammar = grammar;
    }

    protected State getNewState(StateContext context, Grammar.LineMeta meta) {
        return context.getCurrentState();
    }

    public final State process (StateContext context, Grammar.LineMeta meta) {
        if (Grammar.General.COMMENT_BLOCK.equals(meta.matchedPattern)) {
            return COMMENT_BLOCK;
        }

        return getNewState(context, meta);
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public Grammar.LinePattern[] getGrammar() {
        return grammar;
    }

    public void closeScope(StateContext context) {
    }

    public String getAppName() {
        throw new RuntimeException("getAppName should not be used in this ");
    }

    public void setAppName(String appName) {
        throw new RuntimeException("setAppName should not be used in this ");
    }
}
