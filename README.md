# Chunk Loader Persistence

![image](https://i.imgur.com/tJ8pYkS.png)

This mod keeps your chunk loaders running across restarts. It saves any active portal tickets that exist when
the world is shut down and restores them when the world comes back up. No more having to kick start the chunk
loaders across your world to get things working again.

While servers don't shut down often, single-player worlds do and this will be especially handy in that
scenario.

If a portal ticket happened to be from a player passing through, we'll still restore the ticket, but those
tickets only last 30 seconds so it will free the chunk shortly after startup.