package com.flipkart.gap.usl.app.execution;

public enum USLEnvironment {

    LOCAL,
    STAGE,
    PROD;

    public String getId() {
        return this.name().toLowerCase();
    }
}
