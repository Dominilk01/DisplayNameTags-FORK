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

@SuppressWarnings("UnstableApiUsage")
public class NameTagsLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.getContext().getLogger().info("Injecting dependencies");

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
                error.printStackTrace();
            }
        }

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(
            new RemoteRepository.Builder(
                "evoke-games",
                "default",
                "https://maven.pvphub.me/tofaa"
            ).build()
        );
        resolver.addRepository(
            new RemoteRepository.Builder(
                "central",
                "default",
                "https://repo1.maven.org/maven2"
            ).build()
        );

        resolver.addDependency(new Dependency(
            new DefaultArtifact("io.github.tofaa2:spigot:" + entityLibVersion), null, false
        ));

        resolver.addDependency(new Dependency(
            new DefaultArtifact("com.github.ben-manes.caffeine:caffeine:" + caffeineVersion), null, false
        ));

        classpathBuilder.addLibrary(resolver);
    }
}
