package org.bukkit.craftbukkit.generator;

import java.util.List;
import java.util.Random;


import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.craftbukkit.block.CraftBlock;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.biome.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public class CustomChunkGenerator extends NormalChunkGenerator {
    public CustomChunkGenerator(World world, long seeds) {
        super(world,seeds);
    }
}
