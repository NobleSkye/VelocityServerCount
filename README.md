# OnlineServerCount - Velocity Plugin

A Velocity proxy plugin that replaces the player count display in the Minecraft multiplayer server list with the number of backend servers that are currently online.

## 🎯 Features

- **Server Status Display**: Shows online server count instead of player count in the multiplayer list
- **Real-time Monitoring**: Continuously pings backend servers to determine their status
- **Configurable Settings**: Customize ping intervals, timeout values, and ignored servers
- **Async Operations**: All server pings are non-blocking and asynchronous
- **Clean Logging**: Proper error handling and debug logging

## 📋 How it Works

Instead of showing player count like `127/1000 players`, the server list will display:
- **Online Players**: Number of backend servers currently online (e.g., `2` if 2 servers are online)
- **Max Players**: Total number of registered backend servers (e.g., `3` if you have 3 servers total)

**Example**: If you have servers `lobby`, `factions`, and `bedwars` configured, and only `lobby` and `factions` are online, the display will show `2/3` regardless of actual player count.

## ⚙️ Configuration

The plugin creates a `config.properties` file in the plugin's data directory:

```properties
# How often to ping servers (in seconds)
ping_interval_secs=5

# How long until a server is considered offline (in seconds)
offline_timeout_secs=10

# Server names to exclude from the count (comma-separated, optional)
ignore_servers=
```

### Configuration Options

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `ping_interval_secs` | int | 5 | How often to ping servers (seconds) |
| `offline_timeout_secs` | int | 10 | How long until a server is considered offline (seconds) |
| `ignore_servers` | string | empty | Comma-separated list of server names to exclude |

### Example Configuration

```properties
ping_interval_secs=3
offline_timeout_secs=15
ignore_servers=test-server,backup-lobby
```

## 🛠️ Installation

1. **Build the Plugin**:
   ```bash
   ./gradlew shadowJar
   ```

2. **Install**:
   - Copy the generated JAR from `build/libs/` to your Velocity `plugins/` directory
   - Restart your Velocity proxy

3. **Configure** (Optional):
   - Edit `plugins/onlineservercount/config.properties` as needed
   - Restart the proxy to apply changes

## 🔧 Building from Source

### Prerequisites
- Java 17 or higher
- Gradle 7.0 or higher

### Build Steps
```bash
git clone <repository-url>
cd VelocityServerCount
./gradlew shadowJar
```

The compiled plugin will be available in `build/libs/onlineservercount-1.0.0.jar`.

## 📋 Requirements

- **Velocity**: 3.2.0 or higher
- **Java**: 17 or higher
- **Dependencies**: None (plugin is self-contained)

## 🔒 Security & Stability

- ✅ **Async Operations**: All server pings are non-blocking
- ✅ **Privacy**: No server IPs, names, or internal data exposed to clients
- ✅ **Error Handling**: Graceful handling of ping failures and timeouts
- ✅ **Non-intrusive**: Does not interfere with player routing or connection handling
- ✅ **Clean Shutdown**: Properly stops all scheduled tasks on plugin disable

## 🐛 Troubleshooting

### Common Issues

1. **Plugin not loading**:
   - Ensure you're using Velocity 3.2.0+
   - Check the console for any error messages
   - Verify Java 17+ is being used

2. **Server count not updating**:
   - Check if servers are properly registered in Velocity config
   - Verify network connectivity between proxy and backend servers
   - Enable debug logging to see ping results

3. **Configuration not loading**:
   - Ensure `config.properties` syntax is correct
   - Check file permissions in the plugin data directory
   - Review console logs for configuration errors

### Debug Logging

To enable debug logging, add this to your Velocity configuration:
```toml
[advanced]
log-level = "DEBUG"
```

## 📄 License

This project is open source. Feel free to modify and redistribute according to your needs.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

## 📞 Support

If you encounter any issues or have questions:
1. Check the troubleshooting section above
2. Review the console logs for error messages
3. Create an issue with detailed information about your setup and the problem
