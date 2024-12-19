package net.loudcats.wyatt.skidgen.modernworldgen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.loudcats.wyatt.skidgen.modernworldgen.GuiGeneratorSettings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;

public class CavesAndCliffsWorldType extends WorldType {
    public CavesAndCliffsWorldType() {
        super("1.18");
    }

    @Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
        return new SkidGenChunkGenerator(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
    }

    //@Override
    //public WorldChunkManager getChunkManager(World world) {} // i think, maybe, this is where i can introduce biome map parity

    @Override
    public String getTranslateName() {
        return "generator.1.18";  // Key for world type name
    }

    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft instance, GuiCreateWorld guiCreateWorld)
    {
        instance.displayGuiScreen(new GuiGeneratorSettings(guiCreateWorld, guiCreateWorld.field_146334_a));
    }

    public boolean isCustomizable()
    {
        return true;
    }
}
