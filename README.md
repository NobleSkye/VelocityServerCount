# OnlineServerCount

A Velocity plugin that shows your backend server count on the server list instead of player count.

## What It Does

When players view your server in the multiplayer list, they'll see how many of your backend servers are online instead of how many players are online. For example, if you have 3 backend servers (lobby, factions, creative) and 2 are online, it displays "2/3" instead of player counts.

## How It Works

The plugin runs on your Velocity proxy and periodically pings each connected backend server. If a server stops responding for longer than the configured timeout, it's marked as offline. The count updates automatically based on which servers are reachable.

## Configuration

The plugin creates `config.properties` in its data folder:

```properties
ping_interval_secs=5
offline_timeout_secs=10
ignore_servers=
```

- `ping_interval_secs` - How often to ping servers (in seconds)
- `offline_timeout_secs` - How long without response before marking a server offline
- `ignore_servers` - Comma-separated list of servers to exclude from the count

## Installation

1. Build with `mvn package`
2. Copy `target/onlineservercount-x.y.z.jar` to your Velocity plugins folder
3. Restart your proxy

## Requirements

- Velocity 3.0.0+
- Java 17+

## Support

Join the Discord at discord.lionbyte.dev
