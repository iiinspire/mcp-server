package org.foreverland.mcpserver.service;

import org.foreverland.mcpserver.entity.PackageJson;
import org.foreverland.mcpserver.entity.RepositoryConfig;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class PackageAnalyzerService {

    public String identifyFramework(PackageJson packageJson) {
        Map<String, String> deps = packageJson.getDependencies() != null ?
                packageJson.getDependencies() : new HashMap<>();
        Map<String, String> devDeps = packageJson.getDevDependencies() != null ?
                packageJson.getDevDependencies() : new HashMap<>();
        Map<String, String> scripts = packageJson.getScripts() != null ?
                packageJson.getScripts() : new HashMap<>();

        if (deps.containsKey("react")) {
            if (deps.containsKey("next")) return "Next.js";
            if (deps.containsKey("gatsby")) return "Gatsby";
            if (deps.containsKey("react-scripts")) return "Create React App";
            if (devDeps.containsKey("@vitejs/plugin-react")) return "React + Vite";
            return "React";
        }

        if (deps.containsKey("vue")) {
            if (deps.containsKey("nuxt") || deps.containsKey("@nuxt/core")) return "Nuxt.js";
            if (devDeps.containsKey("@vue/cli-service")) return "Vue CLI";
            if (devDeps.containsKey("@vitejs/plugin-vue")) return "Vue + Vite";
            return "Vue.js";
        }

        if (deps.containsKey("@angular/core")) return "Angular";
        if (deps.containsKey("svelte")) return "Svelte";

        if (deps.containsKey("express")) return "Express.js";
        if (deps.containsKey("koa")) return "Koa.js";
        if (deps.containsKey("fastify")) return "Fastify";
        if (deps.containsKey("nest") || deps.containsKey("@nestjs/core")) return "NestJS";
        if (deps.containsKey("hapi") || deps.containsKey("@hapi/hapi")) return "Hapi";

        if (devDeps.containsKey("vite")) return "Vite";
        if (devDeps.containsKey("webpack")) return "Webpack";
        if (devDeps.containsKey("parcel")) return "Parcel";
        if (devDeps.containsKey("rollup")) return "Rollup";

        for (Map.Entry<String, String> script : scripts.entrySet()) {
            String command = script.getValue();
            if (command.contains("next ")) return "Next.js";
            if (command.contains("nuxt ")) return "Nuxt.js";
            if (command.contains("react-scripts ")) return "Create React App";
        }

        return "Unknown framework";
    }

    public RepositoryConfig getBuildConfig(PackageJson packageJson,RepositoryConfig repositoryConfig) {
        String framework = identifyFramework(packageJson);
        String buildCommand = extractBuildCommand(packageJson);
        String outputDir = getOutputDirectory(framework);
        String nodeVersion = getNodeVersion(packageJson, framework);

        repositoryConfig.setBuildCommand(buildCommand);
        repositoryConfig.setFramework(framework);
        repositoryConfig.setOutputDirectory(outputDir);
        repositoryConfig.setNode(nodeVersion);
        return repositoryConfig;
    }

    private String extractBuildCommand(PackageJson packageJson) {
        Map<String, String> scripts = packageJson.getScripts();
        if (scripts == null) {
            return "npm run build";
        }

        if (scripts.containsKey("build")) {
            return "npm run build";
        }

        if (scripts.containsKey("prod") || scripts.containsKey("production")) {
            return scripts.containsKey("prod") ? "npm run prod" : "npm run production";
        }

        return "npm run build";
    }

    private String getOutputDirectory(String framework) {
        Map<String, String> outputDirMap = new HashMap<>();
        outputDirMap.put("React", "build");
        outputDirMap.put("Create React App", "build");
        outputDirMap.put("Vue.js", "dist");
        outputDirMap.put("Vue CLI", "dist");
        outputDirMap.put("Next.js", ".next");
        outputDirMap.put("Nuxt.js", ".output");
        outputDirMap.put("Angular", "dist");
        outputDirMap.put("Svelte", "public");
        outputDirMap.put("Gatsby", "public");
        outputDirMap.put("React + Vite", "dist");
        outputDirMap.put("Vue + Vite", "dist");

        return outputDirMap.getOrDefault(framework, "dist");
    }

    private String getNodeVersion(PackageJson packageJson, String framework) {
        if (packageJson.getEngines() != null && packageJson.getEngines().containsKey("node")) {
            return packageJson.getEngines().get("node");
        }

        Map<String, String> nodeVersionMap = new HashMap<>();
        nodeVersionMap.put("Next.js", "16.x");
        nodeVersionMap.put("Nuxt.js", "16.x");
        nodeVersionMap.put("Angular", "16.x");
        nodeVersionMap.put("React + Vite", "16.x");
        nodeVersionMap.put("Vue + Vite", "16.x");

        return nodeVersionMap.getOrDefault(framework, "14.x");
    }


    private boolean hasDependency(Map<String, String> deps, String dep) {
        return deps != null && deps.containsKey(dep);
    }


    private boolean hasScriptCommand(Map<String, String> scripts, String command) {
        if (scripts == null) return false;

        return scripts.values().stream()
                .anyMatch(script -> script.contains(command));
    }
}