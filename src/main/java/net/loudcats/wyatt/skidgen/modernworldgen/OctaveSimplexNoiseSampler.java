package net.loudcats.wyatt.skidgen.modernworldgen;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OctaveSimplexNoiseSampler {
    private final SimplexNoiseSampler[] octaveSamplers;
    private final double persistence;
    private final double lacunarity;

    public OctaveSimplexNoiseSampler(Random random, IntStream octaves) {
        this(random, octaves.boxed().collect(Collectors.toList()));
    }

    public OctaveSimplexNoiseSampler(Random random, List<Integer> octaves) {
        this(random, (new TreeSet<>(octaves)));
    }

    private OctaveSimplexNoiseSampler(Random random, SortedSet<Integer> octaves) {
        if (octaves.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        } else {
            int i = -octaves.first();
            int j = octaves.last();
            int k = i + j + 1;
            if (k < 1) {
                throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
            } else {
                SimplexNoiseSampler simplexNoiseSampler = new SimplexNoiseSampler(random);
                int l = j;
                this.octaveSamplers = new SimplexNoiseSampler[k];
                if (j >= 0 && j < k && octaves.contains(0)) {
                    this.octaveSamplers[j] = simplexNoiseSampler;
                }

                for(int m = j + 1; m < k; ++m) {
                    if (m >= 0 && octaves.contains(l - m)) {
                        this.octaveSamplers[m] = new SimplexNoiseSampler(random);
                    } else {
//                        random.skip(262);
                    }
                }

                if (j > 0) {
                    long n = (long)(simplexNoiseSampler.sample(simplexNoiseSampler.originX, simplexNoiseSampler.originY, simplexNoiseSampler.originZ) * 9.223372036854776E18D);
                    Random Random = new Random(n);

                    for(int o = l - 1; o >= 0; --o) {
                        if (o < k && octaves.contains(l - o)) {
                            this.octaveSamplers[o] = new SimplexNoiseSampler(Random);
                        } else {
//                            Random.skip(262);
                        }
                    }
                }

                this.lacunarity = Math.pow(2.0D, (double)j);
                this.persistence = 1.0D / (Math.pow(2.0D, (double)k) - 1.0D);
            }
        }
    }

    public double sample(double x, double y, boolean useOrigin) {
        double d = 0.0D;
        double e = this.lacunarity;
        double f = this.persistence;
        SimplexNoiseSampler[] var12 = this.octaveSamplers;
        int var13 = var12.length;

        for(int var14 = 0; var14 < var13; ++var14) {
            SimplexNoiseSampler simplexNoiseSampler = var12[var14];
            if (simplexNoiseSampler != null) {
                d += simplexNoiseSampler.sample(x * e + (useOrigin ? simplexNoiseSampler.originX : 0.0D), y * e + (useOrigin ? simplexNoiseSampler.originY : 0.0D)) * f;
            }

            e /= 2.0D;
            f *= 2.0D;
        }

        return d;
    }

    public double sample(double x, double y, double yScale, double yMax) {
        return this.sample(x, y, true) * 0.55D;
    }
}
