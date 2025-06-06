package org.foreverland.mcpserver;

import org.foreverland.mcpserver.service.CodeDeploymentTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider deployTools(CodeDeploymentTool codeDeploymentTool) {
        return  MethodToolCallbackProvider.builder().toolObjects(codeDeploymentTool).build();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
