package org.foreverland.mcpserver.entity;

import lombok.Data;

@Data
public class RepositoryConfig {

    private String repoId;

    private String name;

    private String framework;

    private String buildCommand;

    private String outputDirectory;

    private String rootDirectory;

    private String installCommand;

    private String developmentCommand;

    private boolean directoryList;

    private String rewrite;

    private String node;

    private String currentBranch;

}
