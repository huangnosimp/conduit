# Conduit
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/vn.io.huangnosimp.conduit?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/vn.io.huangnosimp.conduit)
Status: The plugin is pending approval from the Gradle Plugin Portal.

This Gradle plugin is used to generate Minecraft server source code by remapping and decompiling Mojang's Minecraft server. The plugin only supports Minecraft versions 26.1 and later.

## Requirements

- **JDK 25**
- **Gradle 9.7+**
## How to use

**Kotlin DSL (`build.gradle.kts`):**

```kotlin
plugins {
    id("vn.io.huangnosimp.conduit") version "1.0.0"
}
 
conduit {
    mcVersion = "26.1" // replace with the version you want
}
```

**Groovy DSL (`build.gradle`):**

```groovy
plugins {
    id 'vn.io.huangnosimp.conduit' version '1.0.0'
}
 
conduit {
    mcVersion = '26.1' // replace with the version you want
}
```

Then run:

```bash
./gradlew extractToWorkspace
```

After running it, you will have:

```
src/minecraft/java/
src/minecraft/resources
```

## Task list

All tasks belong to the `conduit` group — run `./gradlew tasks --group conduit` at any time to see this list again.

| Task | Description |
|---|---|
| `downloadMcManifest` | Downloads the list of all available Minecraft versions |
| `downloadVersionManifest` | Downloads the detailed manifest for the configured version |
| `downloadBundleJar` | Downloads the server bundler jar |
| `extractBundleJar` | Extracts `server.jar` and its bundled libraries |
| `remapServerJar` | Remaps `server.jar` to mache's mappings |
| `decompileServerJar` | Decompiles the remapped server jar |
| `applyMachePatches` | Applies the source patches provided by mache |
| `extractToWorkspace` | Exports the source code and resources to the workspace and initializes a Git repository |
