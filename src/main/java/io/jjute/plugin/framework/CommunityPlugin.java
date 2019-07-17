package io.jjute.plugin.framework;

import org.gradle.api.Project;

public class CommunityPlugin implements ProjectPlugin {

    private final String id;
    CommunityPlugin(String id) {
        this.id = id;
    }

    @Override
    public void apply(Project target) {

    }

    @Override
    public String getId() {
        return id;
    }
}
