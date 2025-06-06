package org.foreverland.mcpserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageJson {
    private Map<String, String> dependencies;
    private Map<String, String> devDependencies;
    private Map<String, String> scripts;
    private Map<String, String> engines;

    // Getters and setters
    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, String> getDevDependencies() {
        return devDependencies;
    }

    public void setDevDependencies(Map<String, String> devDependencies) {
        this.devDependencies = devDependencies;
    }

    public Map<String, String> getScripts() {
        return scripts;
    }

    public void setScripts(Map<String, String> scripts) {
        this.scripts = scripts;
    }

    public Map<String, String> getEngines() {
        return engines;
    }

    public void setEngines(Map<String, String> engines) {
        this.engines = engines;
    }
}