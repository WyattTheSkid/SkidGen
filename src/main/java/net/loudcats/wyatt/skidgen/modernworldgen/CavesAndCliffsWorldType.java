package net.loudcats.wyatt.skidgen.modernworldgen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;

public class CavesAndCliffsWorldType extends WorldType {
    public CavesAndCliffsWorldType() {
        super("generator.1.18");
    }

    @Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
        return new SkidGenChunkGenerator(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled());
    }

    @Override
    public String getTranslateName() {
        return "generator.1.18";  // Key for world type name
    }
}
