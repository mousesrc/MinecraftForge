package org.bukkit.craftbukkit.chunkio;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.util.AsynchronousExecutor;

public class ChunkIOExecutor {
    public static Chunk syncChunkLoad(World world, AnvilChunkLoader loader, ChunkProviderServer provider, int x, int z) {
        return net.minecraftforge.common.chunkio.ChunkIOExecutor.syncChunkLoad(world,loader,provider,x,z);
    }

    public static void queueChunkLoad(World world, AnvilChunkLoader loader, ChunkProviderServer provider, int x, int z, Runnable runnable) {
        net.minecraftforge.common.chunkio.ChunkIOExecutor.queueChunkLoad(world,loader,provider,x,z,runnable);
    }

    // Abuses the fact that hashCode and equals for QueuedChunk only use world and coords
    public static void dropQueuedChunkLoad(World world, int x, int z, Runnable runnable) {
        net.minecraftforge.common.chunkio.ChunkIOExecutor.dropQueuedChunkLoad(world,x,z,runnable);
    }

    public static void adjustPoolSize(int players) {
        net.minecraftforge.common.chunkio.ChunkIOExecutor.adjustPoolSize(players);
    }

    public static void tick() {
    }
}
