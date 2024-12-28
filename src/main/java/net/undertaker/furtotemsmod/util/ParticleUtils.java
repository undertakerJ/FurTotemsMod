package net.undertaker.furtotemsmod.util;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;

public class ParticleUtils {
  public static void spawnCircularParticles(
      Level level, double centerX, double centerY, double centerZ, int radius) {
    double step = Math.PI / 64;

    for (double angle = 0; angle < Math.PI * 2; angle += step) {
      double x = centerX + radius * Math.cos(angle);
      double z = centerZ + radius * Math.sin(angle);
      level.addParticle(
          new DustParticleOptions(new Vector3f(0.6F, 0.8F, 1.0F), 1F), x, centerY, z, 0, 0.1, 0);
    }
  }
}
