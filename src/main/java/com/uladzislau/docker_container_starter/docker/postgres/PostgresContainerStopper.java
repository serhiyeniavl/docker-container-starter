package com.uladzislau.docker_container_starter.docker.postgres;

import com.uladzislau.docker_container_starter.config.properties.classes.PostgresInDockerProperties;
import com.uladzislau.docker_container_starter.docker.DockerContainerLogCollector;
import com.uladzislau.docker_container_starter.docker.DockerScriptConfigurator;
import com.uladzislau.docker_container_starter.exec.TerminalScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

@Slf4j
public class PostgresContainerStopper implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        String container = PostgresInDockerProperties.getInstance().getContainer().getName();
        log.info("Container " + container + " stopping started.");
        DockerContainerLogCollector containerLogger = new DockerContainerLogCollector();
        containerLogger
                .collectLog(TerminalScriptExecutor.executeCommand(DockerScriptConfigurator.buildScript(DockerScriptConfigurator.DockerScript.STOP_CONTAINER,
                        container)));
        log.info("Container " + container + " has been stopped.");
    }
}