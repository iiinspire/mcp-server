package org.foreverland.mcpserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class CodeDeploymentTool {

    @Autowired
    private  RestTemplate restTemplate;

    @Value("${foreverland.token}")
    private String foreverLandToken;

    @Value("${foreverland.api.url}")
    private String apiBaseUrl;

    @Tool(name = "deploy_code", description = "deploy code to 4everland")
    public Map<String, Object> deployCode(
            @ToolParam(description = "code_files") Map<String, String> codeFiles,
            @ToolParam(description = "project_name") String name
    ) {
        Path tempDir = null;
        try {
            tempDir = createProjectStructure(name, codeFiles);
            byte[] zipContent = createZipFromDirectory(tempDir.resolve("dist").toFile());

            String projectId = createProject(name);
            String deploymentUrl = deployProject(projectId, zipContent);

            return Map.of(
                    "status", "success",
                    "deploymentUrl", deploymentUrl,
                    "message", "success"
            );
        } catch (Exception e) {
            log.error("Failed to deploy code", e);
            return Map.of(
                    "status", "error",
                    "message", "Failed to deploy: " + e.getMessage()
            );
        } finally {
            cleanupTempDirectory(tempDir);
        }
    }

    private Path createProjectStructure(String name, Map<String, String> codeFiles) throws Exception {
        Path tempDir = Files.createTempDirectory("project-" + name);
        Path distDir = tempDir.resolve("dist");
        Files.createDirectories(distDir);

        for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
            String content = entry.getValue();
            if (content != null) {
                Path filePath = distDir.resolve(entry.getKey());
                Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
            } else {
                log.warn("Content for {} is null", entry.getKey());
            }
        }
        return tempDir;
    }

    private String createProject(String name) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("name", name);
        body.add("mode", 0);
        body.add("platform", "IPFS");
        body.add("deployType", "CLI");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                apiBaseUrl + "/project",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody != null && (int) responseBody.get("code") == 200) {
            Map<String, String> content = (Map<String, String>) responseBody.get("content");
            return content.get("projectId");
        }
        throw new RuntimeException("Failed to create project: " + responseBody);
    }

    private String deployProject(String projectId, byte[] zipContent) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("projectId", projectId);
        body.add("file", new ByteArrayResource(zipContent) {
            @Override
            public String getFilename() {
                return "dist.zip";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                apiBaseUrl + "/deploy",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Deploy response is null");
        }
        return (String) responseBody.get("content");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Accept-Version", "1.0");
        headers.set("token", foreverLandToken);
        return headers;
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir != null) {
            try {
                deleteDirectory(tempDir.toFile());
            } catch (Exception e) {
                log.error("Failed to cleanup temporary directory", e);
            }
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }


    private byte[] createZipFromDirectory(File directory) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addToZip(directory, "", zos);
        }
        return baos.toByteArray();
    }

    private void addToZip(File file, String base, ZipOutputStream zos) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    String path = base.isEmpty() ? child.getName() : base + "/" + child.getName();
                    addToZip(child, path, zos);
                }
            }
        } else {
            ZipEntry entry = new ZipEntry(base);
            zos.putNextEntry(entry);
            Files.copy(file.toPath(), zos);
            zos.closeEntry();
        }
    }
}