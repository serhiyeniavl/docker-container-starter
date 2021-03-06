package com.uladzislau.docker_container_starter.docker.postgres;

import com.uladzislau.docker_container_starter.config.properties.classes.DeveloperProperties;
import com.uladzislau.docker_container_starter.config.properties.constant.PropertiesConstants;
import com.uladzislau.docker_container_starter.docker.ContainerStatus;
import com.uladzislau.docker_container_starter.docker.DockerContainerInspector;
import com.uladzislau.docker_container_starter.docker.DockerScriptConfigurator;
import com.uladzislau.docker_container_starter.docker.LogCollector;
import com.uladzislau.docker_container_starter.exec.TerminalScriptExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresContainerInitializer {
    @SneakyThrows
    public static void initPostgresContainer() {
        log.info("Postgres container initialization started...");

        LogCollector logCollector = new LogCollector();
        DeveloperProperties properties = DeveloperProperties.getInstance();
        DockerContainerInspector containerInspector = new DockerContainerInspector();

        String image = properties.getImage().getName();
        String container = properties.getContainer().getName();
        String logFile = properties.getContainer().getLog_file();

        log.debug("Using image name: " + image);
        log.debug("Using container name: " + container);

        ContainerStatus containerStatus = containerInspector.tryToFind(container);
        switch (containerStatus) {
            case NOT_EXISTS:
                runAndBuildImage(properties, logCollector, image, container);
                break;
            case STOPPED:
                reRunContainer(logCollector, container);
                break;
            default:
        }
        //TODO: Maybe I need get rid of this if block
        if (!logFile.equalsIgnoreCase(PropertiesConstants.NONE)) {
            logCollector.collectInternalContainerLogInFile(container,
                    logFile);
            log.debug("Container " + container + " inner log logged into file " + logFile);
        }
        log.info("Postgres container " + container + " initialization completed.");
    }

    private static void reRunContainer(LogCollector logCollector, String container) {
        log.info("Container with name " + container + " exists. Re-running...");
        logCollector.collectSuccess(TerminalScriptExecutor.executeCommand(DockerScriptConfigurator.buildScript(DockerScriptConfigurator.DockerScript.RE_RUN_CONTAINER, container)));
        log.info("Container " + container + " is up now.");
    }

    private static void runAndBuildImage(DeveloperProperties properties,
                                         LogCollector logCollector,
                                         String image, String container) {
        log.info("Building image " + image + "...");
        logCollector.collectSuccess(TerminalScriptExecutor.executeCommand(DockerScriptConfigurator.buildScript(DockerScriptConfigurator.DockerScript.BUILD_IMAGE,
                image,
                properties.getImage().getDirectory())));
        log.info("Image " + image + " created.");

        log.info("Running container " + container + "...");
        logCollector.collectSuccess(TerminalScriptExecutor.executeCommand(DockerScriptConfigurator.buildScript(DockerScriptConfigurator.DockerScript.RUN_IMAGE,
                container,
                properties.getContainer().getPort(),
                image)));
        log.info("Container " + container + " created.");
    }
}
