package io.jjute.plugin.framework.define;

import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor;

/**
 * Represents a basic {@code ArtifactRepository} model with only skeleton methods
 * providing just enough information to create and resolve popular repositories
 * such as Maven Central, Bintray JCenter and the Google Android repository.
 *
 * @see CommonRepository
 */
@NonNullApi
public interface SimpleArtifactRepository extends ArtifactRepository {

    /**
     * @return The base URL of this repository.
     */
    java.net.URI getUrl();

    @Override
    default void setName(String name) {}

    @Override
    @SuppressWarnings("UnstableApiUsage")
    default void content(Action<? super RepositoryContentDescriptor> configureAction) {}
}
