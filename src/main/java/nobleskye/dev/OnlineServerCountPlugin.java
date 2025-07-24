package nobleskye.dev;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Plugin(
    id = "onlineservercount",
    name = "OnlineServerCount",
    version = "1.0.0",
    description = "Displays the number of online backend servers in the player count",
    authors = {"NobleSkye"}
)
public class OnlineServerCountPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    
    // Configuration
    private int pingIntervalSecs = 5;
    private int offlineTimeoutSecs = 10;
    private List<String> ignoreServers = List.of();
    
    // Server status tracking
    private final Map<String, Long> lastSuccessfulPing = new ConcurrentHashMap<>();
    private ScheduledTask pingTask;

    @Inject
    public OnlineServerCountPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("OnlineServerCount plugin is starting...");
        
        // Load configuration
        loadConfig();
        
        // Start the server ping task
        startPingTask();
        
        logger.info("OnlineServerCount plugin started successfully!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("OnlineServerCount plugin is shutting down...");
        
        // Stop the ping task
        if (pingTask != null) {
            pingTask.cancel();
        }
        
        logger.info("OnlineServerCount plugin shut down successfully!");
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing.Builder builder = event.getPing().asBuilder();
        
        // Calculate online and total servers
        int onlineServers = getOnlineServerCount();
        int totalServers = getTotalServerCount();
        
        // Replace the player count with server count
        builder.onlinePlayers(onlineServers);
        builder.maximumPlayers(totalServers);
        
        event.setPing(builder.build());
        
        logger.debug("ProxyPing: Showing {}/{} servers online", onlineServers, totalServers);
    }

    private void loadConfig() {
        try {
            // Create data directory if it doesn't exist
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            
            Path configPath = dataDirectory.resolve("config.properties");
            
            // Copy default config if it doesn't exist
            if (!Files.exists(configPath)) {
                try (InputStream inputStream = getClass().getResourceAsStream("/config.properties")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, configPath);
                        logger.info("Created default config file at {}", configPath);
                    }
                }
            }
            
            // Load configuration
            Properties properties = new Properties();
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                properties.load(inputStream);
            }
            
            pingIntervalSecs = Integer.parseInt(properties.getProperty("ping_interval_secs", "5"));
            offlineTimeoutSecs = Integer.parseInt(properties.getProperty("offline_timeout_secs", "10"));
            
            String ignoreServersStr = properties.getProperty("ignore_servers", "");
            if (!ignoreServersStr.trim().isEmpty()) {
                ignoreServers = Arrays.asList(ignoreServersStr.split(","));
                // Trim whitespace from server names
                ignoreServers = ignoreServers.stream().map(String::trim).toList();
            }
            
            logger.info("Configuration loaded: ping_interval={}, offline_timeout={}, ignore_servers={}",
                pingIntervalSecs, offlineTimeoutSecs, ignoreServers);
                
        } catch (IOException | NumberFormatException e) {
            logger.error("Failed to load configuration, using defaults", e);
        }
    }

    private void startPingTask() {
        pingTask = server.getScheduler()
            .buildTask(this, this::pingAllServers)
            .repeat(Duration.ofSeconds(pingIntervalSecs))
            .schedule();
        
        logger.info("Started server ping task with interval of {} seconds", pingIntervalSecs);
    }

    private void pingAllServers() {
        long currentTime = System.currentTimeMillis();
        
        for (RegisteredServer registeredServer : server.getAllServers()) {
            String serverName = registeredServer.getServerInfo().getName();
            
            // Skip ignored servers
            if (ignoreServers.contains(serverName)) {
                continue;
            }
            
            // Ping server asynchronously
            registeredServer.ping()
                .orTimeout(3, TimeUnit.SECONDS)
                .whenComplete((ping, throwable) -> {
                    if (throwable == null && ping != null) {
                        // Successful ping
                        lastSuccessfulPing.put(serverName, currentTime);
                        logger.debug("Successfully pinged server: {}", serverName);
                    } else {
                        // Failed ping
                        logger.debug("Failed to ping server: {} - {}", serverName, 
                            throwable != null ? throwable.getMessage() : "No response");
                    }
                });
        }
    }

    private int getOnlineServerCount() {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = offlineTimeoutSecs * 1000L;
        
        return (int) server.getAllServers().stream()
            .map(server -> server.getServerInfo().getName())
            .filter(serverName -> !ignoreServers.contains(serverName))
            .filter(serverName -> {
                Long lastPing = lastSuccessfulPing.get(serverName);
                return lastPing != null && (currentTime - lastPing) <= timeoutMillis;
            })
            .count();
    }

    private int getTotalServerCount() {
        return (int) server.getAllServers().stream()
            .map(server -> server.getServerInfo().getName())
            .filter(serverName -> !ignoreServers.contains(serverName))
            .count();
    }
}
