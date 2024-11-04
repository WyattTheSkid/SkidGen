package net.loudcats.wyatt.skidgen.modernworldgen;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.loudcats.wyatt.skidgen.SkidGen;

import java.util.List;
import java.util.Random;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.*;

public class SkidGenChunkGenerator implements IChunkProvider {
    private final Random random;
    private final World world;
    private final boolean generateStructures;
    private final WorldType worldType;
    private final double[] terrainNoise;
    private final double[] caveNoise;
    private final float[] biomeWeightTable;
    private final NoiseCaveGenerator noiseCaves;
    public NoiseGeneratorOctaves noiseGen5;
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorOctaves mobSpawnerNoise;
    private NoiseGeneratorOctaves field_147431_j;
    private NoiseGeneratorOctaves field_147432_k;
    private NoiseGeneratorOctaves interpolationNoise;
    private NoiseGeneratorPerlin surfaceDepthNoise;
    private double[] surfaceDepthNoises = new double[256];
    private MapGenBase caveGenerator = new MapGenCaves();
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature templeGenerator = new MapGenScatteredFeature();
    private MapGenBase ravineGenerator = new MapGenRavine();
    private BiomeGenBase[] biomes;
    private double[] interpolationNoises;
    private double[] lowerInterpolatedNoises;
    private double[] upperInterpolatedNoises;
    private double[] depthNoises;

    {
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
        strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
        villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
        mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
        templeGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(templeGenerator, SCATTERED_FEATURE);
        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
    }

    public SkidGenChunkGenerator(World world, long seed, boolean generateStructures) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] generating chunks..." );
        }
        this.world = world;
        this.generateStructures = generateStructures;
        this.worldType = world.getWorldInfo().getTerrainType();
        this.random = new Random(seed);
        this.field_147431_j = new NoiseGeneratorOctaves(this.random, 16);
        this.field_147432_k = new NoiseGeneratorOctaves(this.random, 16);
        this.interpolationNoise = new NoiseGeneratorOctaves(this.random, 8);
        this.surfaceDepthNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.noiseGen5 = new NoiseGeneratorOctaves(this.random, 10);
        this.noiseGen6 = new NoiseGeneratorOctaves(this.random, 16);
        this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.random, 8);
        this.terrainNoise = new double[825];
        this.caveNoise = new double[825];
        this.biomeWeightTable = new float[25];
        this.noiseCaves = new NoiseCaveGenerator(this.random);

        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                float f = 10.0F / MathHelper.sqrt_float((float) (j * j + k * k) + 0.2F);
                this.biomeWeightTable[j + 2 + (k + 2) * 5] = f;
            }
        }

        NoiseGenerator[] noiseGens = {field_147431_j, field_147432_k, interpolationNoise, surfaceDepthNoise, noiseGen5, noiseGen6, mobSpawnerNoise};
        noiseGens = TerrainGen.getModdedNoiseGenerators(world, this.random, noiseGens);
        this.field_147431_j = (NoiseGeneratorOctaves) noiseGens[0];
        this.field_147432_k = (NoiseGeneratorOctaves) noiseGens[1];
        this.interpolationNoise = (NoiseGeneratorOctaves) noiseGens[2];
        this.surfaceDepthNoise = (NoiseGeneratorPerlin) noiseGens[3];
        this.noiseGen5 = (NoiseGeneratorOctaves) noiseGens[4];
        this.noiseGen6 = (NoiseGeneratorOctaves) noiseGens[5];
        this.mobSpawnerNoise = (NoiseGeneratorOctaves) noiseGens[6];
    }

    public void populateNoise(int chunkX, int chunkZ, Block[] blocks) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Populating noise..." );
        }
        byte seaLevel = 63;
        this.biomes = this.world.getWorldChunkManager().getBiomesForGeneration(this.biomes, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        this.generateNoise(chunkX * 4, 0, chunkZ * 4);

        for (int noiseX = 0; noiseX < 4; ++noiseX) {
            int ix0 = noiseX * 5;
            int ix1 = (noiseX + 1) * 5;

            for (int noiseZ = 0; noiseZ < 4; ++noiseZ) {
                int ix0z0 = (ix0 + noiseZ) * 33;
                int ix0z1 = (ix0 + noiseZ + 1) * 33;
                int ix1z0 = (ix1 + noiseZ) * 33;
                int ix1z1 = (ix1 + noiseZ + 1) * 33;

                for (int noiseY = 0; noiseY < 32; ++noiseY) {
                    double x0z0 = this.terrainNoise[ix0z0 + noiseY];
                    double x0z1 = this.terrainNoise[ix0z1 + noiseY];
                    double x1z0 = this.terrainNoise[ix1z0 + noiseY];
                    double x1z1 = this.terrainNoise[ix1z1 + noiseY];
                    double x0z0Add = (this.terrainNoise[ix0z0 + noiseY + 1] - x0z0) * 0.125D;
                    double x0z1Add = (this.terrainNoise[ix0z1 + noiseY + 1] - x0z1) * 0.125D;
                    double x1z0Add = (this.terrainNoise[ix1z0 + noiseY + 1] - x1z0) * 0.125D;
                    double x1z1Add = (this.terrainNoise[ix1z1 + noiseY + 1] - x1z1) * 0.125D;

                    for (int pieceY = 0; pieceY < 8; ++pieceY) {
                        double z0 = x0z0;
                        double z1 = x0z1;
                        double z0Add = (x1z0 - x0z0) * 0.25D;
                        double z1Add = (x1z1 - x0z1) * 0.25D;

                        for (int pieceX = 0; pieceX < 4; ++pieceX) {
                            int index = pieceX + noiseX * 4 << 12 | 0 + noiseZ * 4 << 8 | noiseY * 8 + pieceY;
                            short idAdd = 256;
                            index -= idAdd;
                            double densityAdd = (z1 - z0) * 0.25D;
                            double density = z0 - densityAdd;

                            for (int pieceZ = 0; pieceZ < 4; ++pieceZ) {
                                if ((density += densityAdd) > 0.0D) {
                                    blocks[index += idAdd] = Blocks.stone;
                                } else if (noiseY * 8 + pieceY < seaLevel) {
                                    blocks[index += idAdd] = Blocks.water;
                                } else {
                                    // Set to air
                                    blocks[index += idAdd] = null;
                                }
                            }

                            z0 += z0Add;
                            z1 += z1Add;
                        }

                        x0z0 += x0z0Add;
                        x0z1 += x0z1Add;
                        x1z0 += x1z0Add;
                        x1z1 += x1z1Add;
                    }
                }
            }
        }
    }
    public void buildSurface(int chunkX, int chunkZ, Block[] blocks, byte[] meta, BiomeGenBase[] biomes) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Building surfacee..." );
        }
        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, chunkX, chunkZ, blocks, meta, biomes, this.world);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) return;

        double scale = 0.03125D;
        this.surfaceDepthNoises = this.surfaceDepthNoise.func_151599_a(this.surfaceDepthNoises, (double) (chunkX * 16), (double) (chunkZ * 16), 16, 16, scale * 2.0D, scale * 2.0D, 1.0D);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BiomeGenBase biomegenbase = biomes[z + x * 16];
                biomegenbase.genTerrainBlocks(this.world, this.random, blocks, meta, chunkX * 16 + x, chunkZ * 16 + z, this.surfaceDepthNoises[z + x * 16]);
            }
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int chunkX, int chunkZ) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Attempting to load chunk..." );
        }
        return this.provideChunk(chunkX, chunkZ);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int chunkX, int chunkZ) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Providing a chunk..." );
        }
        this.random.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        Block[] blocks = new Block[65536];
        byte[] meta = new byte[65536];
        this.populateNoise(chunkX, chunkZ, blocks);

        this.biomes = this.world.getWorldChunkManager().loadBlockGeneratorData(this.biomes, chunkX * 16, chunkZ * 16, 16, 16);
        this.buildSurface(chunkX, chunkZ, blocks, meta, this.biomes);

        this.generateNoiseCaves(chunkX, chunkZ, blocks, meta);

        this.caveGenerator.func_151539_a(this, this.world, chunkX, chunkZ, blocks);
        this.ravineGenerator.func_151539_a(this, this.world, chunkX, chunkZ, blocks);

        if (this.generateStructures) {
            this.mineshaftGenerator.func_151539_a(this, this.world, chunkX, chunkZ, blocks);
            this.villageGenerator.func_151539_a(this, this.world, chunkX, chunkZ, blocks);
            this.strongholdGenerator.func_151539_a(this, this.world, chunkX, chunkZ, blocks);
            this.templeGenerator.func_151539_a(this, this.world, chunkX, chunkZ, blocks);
        }

        Chunk chunk = new Chunk(this.world, blocks, meta, chunkX, chunkZ);
        byte[] biomes = chunk.getBiomeArray();

        for (int i = 0; i < biomes.length; ++i) {
            biomes[i] = (byte) this.biomes[i].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private void generateNoiseCaves(int chunkX, int chunkZ, Block[] blocks, byte[] meta) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Generating cave noise..." );
        }
        generateNoiseCavesNoise(chunkX, chunkZ);

        for (int noiseX = 0; noiseX < 4; ++noiseX) {
            int ix0 = noiseX * 5;
            int ix1 = (noiseX + 1) * 5;

            for (int noiseZ = 0; noiseZ < 4; ++noiseZ) {
                int ix0z0 = (ix0 + noiseZ) * 33;
                int ix0z1 = (ix0 + noiseZ + 1) * 33;
                int ix1z0 = (ix1 + noiseZ) * 33;
                int ix1z1 = (ix1 + noiseZ + 1) * 33;

                for (int noiseY = 0; noiseY < 32; ++noiseY) {
                    double x0z0 = this.caveNoise[ix0z0 + noiseY];
                    double x0z1 = this.caveNoise[ix0z1 + noiseY];
                    double x1z0 = this.caveNoise[ix1z0 + noiseY];
                    double x1z1 = this.caveNoise[ix1z1 + noiseY];
                    double x0z0Add = (this.caveNoise[ix0z0 + noiseY + 1] - x0z0) * 0.125D;
                    double x0z1Add = (this.caveNoise[ix0z1 + noiseY + 1] - x0z1) * 0.125D;
                    double x1z0Add = (this.caveNoise[ix1z0 + noiseY + 1] - x1z0) * 0.125D;
                    double x1z1Add = (this.caveNoise[ix1z1 + noiseY + 1] - x1z1) * 0.125D;

                    for (int pieceY = 0; pieceY < 8; ++pieceY) {
                        double z0 = x0z0;
                        double z1 = x0z1;
                        double z0Add = (x1z0 - x0z0) * 0.25D;
                        double z1Add = (x1z1 - x0z1) * 0.25D;

                        for (int pieceX = 0; pieceX < 4; ++pieceX) {
                            int index = pieceX + noiseX * 4 << 12 | 0 + noiseZ * 4 << 8 | noiseY * 8 + pieceY;
                            short idAdd = 256;
                            index -= idAdd;
                            double densityAdd = (z1 - z0) * 0.25D;
                            double density = z0 - densityAdd;

                            for (int pieceZ = 0; pieceZ < 4; ++pieceZ) {
                                index += idAdd;
                                if ((density += densityAdd) < 0.0D && blocks[index] == Blocks.stone) {
                                    if (noiseY * 8 + pieceY < 11) {
                                        blocks[index] = Blocks.lava;
                                    } else {
                                        blocks[index] = null;
                                    }
                                }
                            }

                            z0 += z0Add;
                            z1 += z1Add;
                        }

                        x0z0 += x0z0Add;
                        x0z1 += x0z1Add;
                        x1z0 += x1z0Add;
                        x1z1 += x1z1Add;
                    }
                }
            }
        }
    }

    private void generateNoiseCavesNoise(int chunkX, int chunkZ) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Generating more cave noise..." );
        }
        BiomeGenBase[] biomes = null;
        biomes = this.world.getWorldChunkManager().getBiomesForGeneration(biomes, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        int i = 0;

        for (int x = 0; x < 5; ++x) {
            for (int z = 0; z < 5; ++z) {
                double lowestScaledDepth = 0;

                // We iterate the entire area to ensure we're not anywhere even near an ocean
                for (int x1 = -2; x1 <= 2; ++x1) {
                    for (int z1 = -2; z1 <= 2; ++z1) {
                        BiomeGenBase biome = biomes[x + x1 + 2 + (z + z1 + 2) * 10];

                        // Disable in oceans
                        lowestScaledDepth = Math.min(lowestScaledDepth, biome.rootHeight);
                    }
                }

                // Each unit of depth roughly corresponds to 16 blocks, but we use 20 for good measure
                // We start reduction at 56 instead of 64, the sea level, to give ourselves some more room.
                double startLevel = 56 + (lowestScaledDepth * 20);
                int sub = (int) (startLevel / 8);

                for (int y = 0; y < 33; y++) {
                    double caveNoise = this.noiseCaves.sample(this.terrainNoise[i], y * 8, chunkZ * 16 + (z * 4), chunkX * 16 + (x * 4));

                    // Reduce so we don't break the surface
                    caveNoise = net.loudcats.wyatt.skidgen.modernworldgen.MathHelper.clampedLerp(caveNoise, (lowestScaledDepth * -30) + 20, (y - sub + 2) / 2.0);

                    this.caveNoise[i] = caveNoise;
                    i++;
                }
            }
        }
    }

    private void generateNoise(int x, int y, int z) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Generating noise..." );
        }
        this.depthNoises = this.noiseGen6.generateNoiseOctaves(this.depthNoises, x, z, 5, 5, 200.0D, 200.0D, 0.5D);
        this.interpolationNoises = this.interpolationNoise.generateNoiseOctaves(this.interpolationNoises, x, y, z, 5, 33, 5, 8.555150000000001D, 4.277575000000001D, 8.555150000000001D);
        this.lowerInterpolatedNoises = this.field_147431_j.generateNoiseOctaves(this.lowerInterpolatedNoises, x, y, z, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        this.upperInterpolatedNoises = this.field_147432_k.generateNoiseOctaves(this.upperInterpolatedNoises, x, y, z, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        int index = 0;
        int horizontalIndex = 0;

        for (int pieceX = 0; pieceX < 5; ++pieceX) {
            for (int pieceZ = 0; pieceZ < 5; ++pieceZ) {
                float scale = 0.0F;
                float depth = 0.0F;
                float weight = 0.0F;
                byte radius = 2;
                BiomeGenBase biomegenbase = this.biomes[pieceX + 2 + (pieceZ + 2) * 10];

                for (int x1 = -radius; x1 <= radius; ++x1) {
                    for (int z1 = -radius; z1 <= radius; ++z1) {
                        BiomeGenBase biomegenbase1 = this.biomes[pieceX + x1 + 2 + (pieceZ + z1 + 2) * 10];
                        float depthHere = biomegenbase1.rootHeight;
                        float scaleHere = biomegenbase1.heightVariation;

                        if (this.worldType == WorldType.AMPLIFIED && depthHere > 0.0F) {
                            depthHere = 1.0F + depthHere * 2.0F;
                            scaleHere = 1.0F + scaleHere * 4.0F;
                        }

                        float weightHere = this.biomeWeightTable[x1 + 2 + (z1 + 2) * 5] / (depthHere + 2.0F);

                        if (biomegenbase1.rootHeight > biomegenbase.rootHeight) {
                            weightHere /= 2.0F;
                        }

                        scale += scaleHere * weightHere;
                        depth += depthHere * weightHere;
                        weight += weightHere;
                    }
                }

                scale /= weight;
                depth /= weight;
                scale = scale * 0.9F + 0.1F;
                depth = (depth * 4.0F - 1.0F) / 8.0F;
                double depthNoise = this.depthNoises[horizontalIndex] / 8000.0D;

                if (depthNoise < 0.0D) {
                    depthNoise = -depthNoise * 0.3D;
                }

                depthNoise = depthNoise * 3.0D - 2.0D;

                if (depthNoise < 0.0D) {
                    depthNoise /= 2.0D;

                    if (depthNoise < -1.0D) {
                        depthNoise = -1.0D;
                    }

                    depthNoise /= 1.4D;
                    depthNoise /= 2.0D;
                } else {
                    if (depthNoise > 1.0D) {
                        depthNoise = 1.0D;
                    }

                    depthNoise /= 8.0D;
                }

                ++horizontalIndex;
                double scaledDepth = (double) depth;
                double scaledScale = (double) scale;
                scaledDepth += depthNoise * 0.2D;
                scaledDepth = scaledDepth * 8.5D / 8.0D;
                double terrainHeight = 8.5D + scaledDepth * 4.0D;

                for (int pieceY = 0; pieceY < 33; ++pieceY) {
                    double falloff = ((double) pieceY - terrainHeight) * 12.0D * 128.0D / 256.0D / scaledScale;

                    if (falloff < 0.0D) {
                        falloff *= 4.0D;
                    }

                    double lowerNoise = this.lowerInterpolatedNoises[index] / 512.0D;
                    double upperNoise = this.upperInterpolatedNoises[index] / 512.0D;
                    double interpolation = (this.interpolationNoises[index] / 10.0D + 1.0D) / 2.0D;
                    double noise = MathHelper.denormalizeClamp(lowerNoise, upperNoise, interpolation) - falloff;

                    // Scale down the last 3 layers
                    if (pieceY > 29) {
                        double lerp = (double) ((float) (pieceY - 29) / 3.0F);
                        noise = noise * (1.0D - lerp) + -10.0D * lerp;
                    }

                    this.terrainNoise[index] = noise;
                    ++index;
                }
            }
        }
    }

    public boolean chunkExists(int chunkX, int chunkZ) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Checking if chunk exists..." );
        }
        return true;
    }

    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Populating..." );
        }
        BlockFalling.fallInstantly = true;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(x + 16, z + 16);
        this.random.setSeed(this.world.getSeed());
        long longA = this.random.nextLong() / 2L * 2L + 1L;
        long longB = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long) chunkX * longA + (long) chunkZ * longB ^ this.world.getSeed());
        boolean generatedVillage = false;

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(chunkProvider, world, random, chunkX, chunkZ, generatedVillage));

        if (this.generateStructures) {
            this.mineshaftGenerator.generateStructuresInChunk(this.world, this.random, chunkX, chunkZ);
            generatedVillage = this.villageGenerator.generateStructuresInChunk(this.world, this.random, chunkX, chunkZ);
            this.strongholdGenerator.generateStructuresInChunk(this.world, this.random, chunkX, chunkZ);
            this.templeGenerator.generateStructuresInChunk(this.world, this.random, chunkX, chunkZ);
        }

        int x1;
        int y1;
        int z1;

        if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && !generatedVillage && this.random.nextInt(4) == 0
                && TerrainGen.populate(chunkProvider, world, random, chunkX, chunkZ, generatedVillage, LAKE)) {
            x1 = x + this.random.nextInt(16) + 8;
            y1 = this.random.nextInt(256);
            z1 = z + this.random.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.water)).generate(this.world, this.random, x1, y1, z1);
        }

        if (TerrainGen.populate(chunkProvider, world, random, chunkX, chunkZ, generatedVillage, LAVA) && !generatedVillage && this.random.nextInt(8) == 0) {
            x1 = x + this.random.nextInt(16) + 8;
            y1 = this.random.nextInt(this.random.nextInt(248) + 8);
            z1 = z + this.random.nextInt(16) + 8;

            if (y1 < 63 || this.random.nextInt(10) == 0) {
                (new WorldGenLakes(Blocks.lava)).generate(this.world, this.random, x1, y1, z1);
            }
        }

        boolean doGen = TerrainGen.populate(chunkProvider, world, random, chunkX, chunkZ, generatedVillage, DUNGEON);
        for (x1 = 0; doGen && x1 < 8; ++x1) {
            y1 = x + this.random.nextInt(16) + 8;
            z1 = this.random.nextInt(256);
            int j2 = z + this.random.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(this.world, this.random, y1, z1, j2);
        }

        biomegenbase.decorate(this.world, this.random, x, z);
        if (TerrainGen.populate(chunkProvider, world, random, chunkX, chunkZ, generatedVillage, ANIMALS)) {
            SpawnerAnimals.performWorldGenSpawning(this.world, biomegenbase, x + 8, z + 8, 16, 16, this.random);
        }
        x += 8;
        z += 8;

        doGen = TerrainGen.populate(chunkProvider, world, random, chunkX, chunkZ, generatedVillage, ICE);
        for (x1 = 0; doGen && x1 < 16; ++x1) {
            for (y1 = 0; y1 < 16; ++y1) {
                z1 = this.world.getPrecipitationHeight(x + x1, z + y1);

                if (this.world.isBlockFreezable(x1 + x, z1 - 1, y1 + z)) {
                    this.world.setBlock(x1 + x, z1 - 1, y1 + z, Blocks.ice, 0, 2);
                }

                if (this.world.func_147478_e(x1 + x, z1, y1 + z, true)) {
                    this.world.setBlock(x1 + x, z1, y1 + z, Blocks.snow_layer, 0, 2);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(chunkProvider, world, random, chunkX, chunkZ, generatedVillage));

        BlockFalling.fallInstantly = false;
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean saveExtra, IProgressUpdate update) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Saving chunks..." );
        }
        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData() {
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks() {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave() {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString() {
        return "SkidGenChunkGenerator";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_) {
        BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(p_73155_2_, p_73155_4_);
        return p_73155_1_ == EnumCreatureType.monster && this.templeGenerator.func_143030_a(p_73155_2_, p_73155_3_, p_73155_4_) ? this.templeGenerator.getScatteredFeatureSpawnList() : biomegenbase.getSpawnableList(p_73155_1_);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
        return "Stronghold".equals(p_147416_2_) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(p_147416_1_, p_147416_3_, p_147416_4_, p_147416_5_) : null;
    }

    public int getLoadedChunkCount() {
        return 0;
    }

    public void recreateStructures(int chunkX, int chunkZ) {
        if (SkidGen.isDebug()) {
            System.out.println("[SkidGen] Recreating structures..." );
        }
        if (this.generateStructures) {
            this.mineshaftGenerator.func_151539_a(this, this.world, chunkX, chunkZ, null);
            this.villageGenerator.func_151539_a(this, this.world, chunkX, chunkZ, null);
            this.strongholdGenerator.func_151539_a(this, this.world, chunkX, chunkZ, null);
            this.templeGenerator.func_151539_a(this, this.world, chunkX, chunkZ, null);
        }
    }
}
