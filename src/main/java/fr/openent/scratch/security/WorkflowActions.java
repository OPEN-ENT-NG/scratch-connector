package fr.openent.scratch.security;

import fr.openent.scratch.Scratch;

public enum WorkflowActions {
    ACCESS_RIGHT (Scratch.ACCESS_RIGHT);

    private final String actionName;

    WorkflowActions(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString () {
        return this.actionName;
    }
}
