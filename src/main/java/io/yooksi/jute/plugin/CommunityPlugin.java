package io.yooksi.jute.plugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;

public enum CommunityPlugin implements ProjectPlugin {

    TEST(null);

    private final String id;
    CommunityPlugin(String id) {
        this.id = id;
    }

    @Override
    public void apply(Project target) {

    }

    @Override
    public String getId() {
        return null;
    }
}
