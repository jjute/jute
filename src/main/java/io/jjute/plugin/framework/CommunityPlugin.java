package io.jjute.plugin.framework;

import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class CommunityPlugin implements ProjectPlugin {

    private final String id;
    CommunityPlugin(String id) {
        this.id = id;
    }

    @Override
    public void apply(@NotNull Project target) {

    }

    @Override
    public String getId() {
        return id;
    }
}
