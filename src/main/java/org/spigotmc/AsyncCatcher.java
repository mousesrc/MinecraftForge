package org.spigotmc;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

public class AsyncCatcher
{

    public static boolean enabled = true;

    public static void catchOp(String reason)
    {
        if ( enabled && Thread.currentThread() != ((CraftServer)Bukkit.getServer()).getServer().primaryThread )
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
