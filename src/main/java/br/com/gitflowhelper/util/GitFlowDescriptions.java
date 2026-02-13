package br.com.gitflowhelper.util;

public enum GitFlowDescriptions {

    INIT("Initializes current repo to use Git Flow (git flow init)."),
    REPO_GROUP("Checkout or delete local or remote branches."),
    REPO_ITEM("Checkout or delete this branch."),

    FEATURE_GROUP("Feature related commands."),
    RELEASE_GROUP("Release related commands."),
    HOTFIX_GROUP("Bugs and hotfix related commands."),

    FEATURE_START("Creates a new local feature branch (git flow feature start)."),
    FEATURE_PUBLISH("Pushes the current feature branch to VCS (git flow feature publish)."),
    FEATURE_FINISH("Merges current feature branch into develop chain and deletes current branch or creates a merge request (git flow feature finish)."),

    RELEASE_START("Creates a new local release branch (git flow release start)."),
    RELEASE_PUBLISH("Pushes the current release branch to VCS (git flow release publish)."),
    RELEASE_FINISH("Merges current release branch into master chain and deletes current branch (git flow release finish)."),


    HOTFIX_START("Creates a new local hotfix branch (git flow hotfix start)."),
    HOTFIX_PUBLISH("Pushes the current hotfix branch to VCS (git flow hotfix publish)."),
    HOTFIX_FINISH("Merges current hotfix branch into master and develop chains and deletes current branch (git flow hotfix finish).");


    private final String value;

    GitFlowDescriptions(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GitFlowDescriptions fromValue(String value) {
        for (GitFlowDescriptions desc : values()) {
            if (desc.value.equalsIgnoreCase(value)) {
                return desc;
            }
        }
        throw new IllegalArgumentException("Unknown description: " + value);
    }
}
