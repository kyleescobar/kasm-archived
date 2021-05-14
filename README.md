# KASM
A Kotlin bytecode modification library developed specifically for use within other Spectral Powered 
projects. This library adds a nicer Kotlin-Friendly API while still extending the OW2 ASM library.

## Getting Started
Using KASM in your own project is very simple. Follow the example below on how to add this project
to your existing Gradle or maven project.

```groovy
repositories {
    maven { url "https://maven.spectralpowered.org" }
}

dependencies {
    implementation "org.spectralpowered.kasm:kasm:1.0.0"
    implementation "org.spectralpowered.kasm:commons:1.0.0"
    implementation "org.spectralpowered.kasm:executor:1.0.0"
}
```
