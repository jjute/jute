package io.jjute.plugin.framework;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class CommunityPlugin implements ProjectPlugin {

    private final String id;
    private final String version;

    public CommunityPlugin(String id, String version) {
        this.id = id; this.version = version;
    }

    public CommunityPlugin(String id) {
        this.id = id; version = StringUtils.EMPTY;
    }

    @Override
    public void apply(@NotNull Project target) {

    }

    @Override
    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "id \"" + getId() + "\"" + (!version.isEmpty() ?
                " version " + "\"" + getVersion() + "\"" : StringUtils.EMPTY);
    }
}
