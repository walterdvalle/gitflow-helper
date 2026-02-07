package br.com.gitflowhelper.util;

public enum GitFlowBranchType {

    FEATURE("feature"),
    RELEASE("release"),
    HOTFIX("hotfix");

    private final String value;

    GitFlowBranchType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GitFlowBranchType fromValue(String value) {
        for (GitFlowBranchType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown branch type: " + value);
    }
}
