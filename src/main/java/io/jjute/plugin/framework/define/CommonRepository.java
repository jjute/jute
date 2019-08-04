package io.jjute.plugin.framework.define;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Popular and publicly-available repositories used around the world.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/declaring_repositories.html">
 *      Gradle Docs: Declaring Repositories</a>
 */
@NonNullApi
public enum CommonRepository implements SimpleArtifactRepository {

    JCENTER("maven", DefaultRepositoryHandler.BINTRAY_JCENTER_URL),
    MAVEN_CENTRAL("maven", ArtifactRepositoryContainer.MAVEN_CENTRAL_URL),
    GOOGLE("maven", ArtifactRepositoryContainer.GOOGLE_URL);

    private final String name;
    private final URI url;

    CommonRepository(String name, String url) {
        this.name = name;
        try {
            this.url = new URI(url);
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getUrl() {
        return url;
    }
}
