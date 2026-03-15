package com.mattmx.nametags;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class NameTagsLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.getContext().getLogger().info("Injecting nametags dependencies");

        final File override = classpathBuilder.getContext()
            .getDataDirectory()
            .resolve(".override")
            .toFile();

        String entityLibVersion = "3.1.0-SNAPSHOT";
        String caffeineVersion = "3.2.0";

        if (override.exists()) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(override));
                entityLibVersion = props.getProperty("entityLib", entityLibVersion);
                caffeineVersion = props.getProperty("caffeine", caffeineVersion);
            } catch (Exception error) {
                classpathBuilder.getContext().getLogger().warn("Could not load '.override' file using defaults", error);
            }
        }

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        // entityLib
        resolver.addRepository(
            new RemoteRepository.Builder(
                "evoke-games",
                "default",
                "https://maven.pvphub.me/tofaa"
            ).build()
        );

        resolver.addDependency(new Dependency(
            new DefaultArtifact("io.github.tofaa2:spigot:" + entityLibVersion), null, false
        ));

        // caffeine
        resolver.addRepository(
            new RemoteRepository.Builder(
                "central",
                "default",
                getDefaultMavenCentralMirror()
            ).build()
        );

        resolver.addDependency(new Dependency(
            new DefaultArtifact("com.github.ben-manes.caffeine:caffeine:" + caffeineVersion), null, false
        ));

        classpathBuilder.addLibrary(resolver);
    }

    // the plugin use paper 1.21.5-R0.1-SNAPSHOT witch don't include MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
    // Using what's intended so newer version don't get exception
    // source : https://github.com/PaperMC/Paper/blob/main/paper-api/src/main/java/io/papermc/paper/plugin/loader/library/impl/MavenLibraryResolver.java#L155
    private static String getDefaultMavenCentralMirror() {
        String central = System.getenv("PAPER_DEFAULT_CENTRAL_REPOSITORY");
        if (central == null) {
            central = System.getProperty("org.bukkit.plugin.java.LibraryLoader.centralURL");
        }
        if (central == null) {
            central = "https://maven-central.storage-download.googleapis.com/maven2";
        }
        return central;

    }
}
