package org.spigotmc;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;

public class TicksPerSecondCommand extends Command
{

    public TicksPerSecondCommand(String name)
    {
        super( name );
        this.description = "Gets the current ticks per second for the server";
        this.usageMessage = "/tps";
        this.setPermission( "bukkit.command.tps" );
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args)
    {
        if ( !testPermission( sender ) )
        {
            return true;
        }

        StringBuilder sb = new StringBuilder( ChatColor.GOLD + "TPS from last 1m, 5m, 15m: " );
        double[] tps =  ((CraftServer) Bukkit.getServer()).getServer().recentTps;
        double[] tps1 = {tps[0],(tps[0] + tps[1]) / 2, (tps[0] + tps[1] + tps[2]) / 3};
        for ( double d : tps1 )
        {
            sb.append( format( d ) );
            sb.append( ", " );
        }
        sender.sendMessage( sb.substring( 0, sb.length() - 2 ) );

        return true;
    }

    private String format(double tps)
    {
        return ( ( tps > 18.0 ) ? ChatColor.GREEN : ( tps > 16.0 ) ? ChatColor.YELLOW : ChatColor.RED ).toString()
                + ( ( tps > 20.0 ) ? "*" : "" ) + Math.min( Math.round( tps * 100.0 ) / 100.0, 20.0 );
    }
}
