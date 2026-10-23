package dev.arcanerealms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Pretty, non-destructive spell effects. All world changes run on the logical server. */
public final class SpellEffects {
    private SpellEffects() {}

    public static void cast(ServerLevel level, ServerPlayer caster, StaffItem.Element element, int spell) {
        switch (element) {
            case ASTRAL -> castAstral(level, caster, spell);
            case EMBER -> castEmber(level, caster, spell);
            case FROST -> castFrost(level, caster, spell);
        }
        // Every spell gets a shared burst of matching particles at the caster's hands.
        ring(level, caster.position().add(0, 1.1, 0), 1.1, 36, dust(element.rgb, 1.15F));
    }

    private static void castAstral(ServerLevel level, ServerPlayer player, int spell) {
        switch (spell) {
            case 0 -> { // Starstep: blink up to eight blocks, never through unsafe feet/head spaces.
                Vec3 oldPos = player.position();
                Vec3 look = player.getLookAngle();
                Vec3 flat = new Vec3(look.x, 0, look.z);
                if (flat.lengthSqr() < 0.0001) flat = new Vec3(0, 0, 1);
                flat = flat.normalize();
                boolean teleported = false;
                for (int distance = 8; distance >= 2; distance--) {
                    Vec3 candidate = oldPos.add(flat.scale(distance));
                    BlockPos feet = BlockPos.containing(candidate);
                    if (level.getBlockState(feet).isAir()
                            && level.getBlockState(feet.above()).isAir()
                            && !level.getBlockState(feet.below()).isAir()
                            && level.getFluidState(feet).isEmpty()) {
                        spiral(level, oldPos.add(0, 1, 0), 0.75, 3, 36, ParticleTypes.PORTAL);
                        player.teleportTo(candidate.x, candidate.y, candidate.z);
                        spiral(level, candidate.add(0, 1, 0), 0.75, 3, 36, ParticleTypes.END_ROD);
                        teleported = true;
                        break;
                    }
                }
                if (!teleported) {
                    // Still visibly cast, but refuse unsafe teleport destinations.
                    spiral(level, oldPos.add(0, 1, 0), 0.8, 2, 26, ParticleTypes.PORTAL);
                }
            }
            case 1 -> { // Astral Mend: instant healing and a soft green-gold glow.
                player.heal(7.0F);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                burst(level, player.position().add(0, 1.0, 0), ParticleTypes.HEART, 26, 1.15);
                spiral(level, player.position().add(0, 0.4, 0), 0.85, 2.6, 60, ParticleTypes.END_ROD);
            }
            case 2 -> { // Cosmic Halo: absorption shield and starburst helix.
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 25, 1));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 9, 0));
                for (int height = 0; height < 4; height++) {
                    ring(level, player.position().add(0, 0.5 + height * 0.6, 0),
                            1.6 + height * 0.08, 40, dust(0xAA65FF, 1.0F));
                }
                burst(level, player.position().add(0, 2, 0), ParticleTypes.FIREWORK, 28, 1.2);
            }
            case 3 -> { // Starlight Sight: night vision and clear navigation.
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 100, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 15, 0));
                skyBeam(level, player, ParticleTypes.END_ROD, 12.0);
            }
            default -> { }
        }
    }

    private static void castEmber(ServerLevel level, ServerPlayer player, int spell) {
        switch (spell) {
            case 0 -> { // Phoenix Leap: boosted jump, cushioned landing.
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(player.getDeltaMovement().add(look.x * 0.65, 0.85, look.z * 0.65));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 8, 0));
                burst(level, player.position().add(0, 0.6, 0), ParticleTypes.FLAME, 75, 1.0);
                ring(level, player.position().add(0, 0.2, 0), 1.8, 50, dust(0xFFB33D, 1.2F));
            }
            case 1 -> { // Flameward: immune to lava/fire damage temporarily.
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 40, 0));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 8, 0));
                spiral(level, player.position().add(0, 0.1, 0), 1.2, 3, 90, ParticleTypes.FLAME);
            }
            case 2 -> { // Solar Flare: bright light spectacle, reveals nearby creatures.
                Vec3 p = player.position().add(0, 1.2, 0);
                for (int r = 1; r <= 4; r++) ring(level, p, r, 64, dust(0xFF9B28, 1.15F));
                burst(level, p, ParticleTypes.FIREWORK, 80, 2.8);
                AABB search = player.getBoundingBox().inflate(11.0);
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, search, e -> e != player)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 15, 0));
                }
            }
            case 3 -> { // Blazing Stride: swiftness and a fire-colored pathway.
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 24, 2));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 24, 1));
                Vec3 look = player.getLookAngle();
                for (int i = 0; i < 10; i++) {
                    double dist = i * 0.65;
                    Vec3 p = player.position().add(look.x * dist, 0.2, look.z * dist);
                    burst(level, p, ParticleTypes.SMALL_FLAME, 5, 0.22);
                }
            }
            default -> { }
        }
    }

    private static void castFrost(ServerLevel level, ServerPlayer player, int spell) {
        switch (spell) {
            case 0 -> { // Frost Bridge: turn water into temporary frosted ice nearby.
                int frozen = 0;
                BlockPos base = player.blockPosition().below();
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        if (dx * dx + dz * dz > 10) continue;
                        BlockPos pos = base.offset(dx, 0, dz);
                        if (level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).isAir()) {
                            level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                            level.scheduleTick(pos, Blocks.FROSTED_ICE, 80 + level.getRandom().nextInt(60));
                            frozen++;
                        }
                    }
                }
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 14, 0));
                ring(level, player.position().add(0, 0.1, 0), 3.1, 64, ParticleTypes.SNOWFLAKE);
                if (frozen == 0) burst(level, player.position(), ParticleTypes.SNOWFLAKE, 35, 1.7);
            }
            case 1 -> { // Snow Globe: swirling blizzard, gentle falling for the caster.
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 20, 0));
                for (int i = 0; i < 5; i++) {
                    ring(level, player.position().add(0, 0.6 + i * 0.55, 0), 2.3, 48, ParticleTypes.SNOWFLAKE);
                }
                spiral(level, player.position().add(0, 0, 0), 2.2, 4, 110, dust(0x86E8FF, 0.9F));
            }
            case 2 -> { // Winter Ward: cold-blue shield and gentle descent.
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 24, 1));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 24, 0));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 24, 0));
                ring(level, player.position().add(0, 1, 0), 1.9, 66, dust(0x55DFFF, 1.4F));
                burst(level, player.position().add(0, 1.1, 0), ParticleTypes.GLOW, 36, 1.6);
            }
            case 3 -> { // Crystal Rush: speed boost and shimmering falling crystals.
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 20, 1));
                skyBeam(level, player, ParticleTypes.SNOWFLAKE, 10.0);
                burst(level, player.position().add(0, 0.6, 0), ParticleTypes.END_ROD, 40, 1.7);
            }
            default -> { }
        }
    }

    public static void ambient(ServerLevel level, ServerPlayer player, StaffItem.Element element, int tick) {
        double angle = tick * 0.18 + (player.getId() % 7);
        double radius = 0.7;
        Vec3 center = player.position().add(0, 1.0, 0);
        for (int i = 0; i < 3; i++) {
            double a = angle + i * Math.PI * 2 / 3;
            Vec3 at = center.add(Math.cos(a) * radius, 0.35 * Math.sin(a * 0.6), Math.sin(a) * radius);
            level.sendParticles(dust(element.rgb, 0.8F), at.x, at.y, at.z, 1, 0, 0, 0, 0);
        }
    }

    private static DustParticleOptions dust(int rgb, float size) {
        return new DustParticleOptions(rgb, size);
    }

    private static void ring(ServerLevel level, Vec3 center, double radius, int points, ParticleOptions particle) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y,
                    center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
    }

    private static void spiral(ServerLevel level, Vec3 base, double radius, double height,
                               int points, ParticleOptions particle) {
        for (int i = 0; i < points; i++) {
            double t = i / (double) points;
            double a = t * Math.PI * 6;
            level.sendParticles(particle, base.x + Math.cos(a) * radius, base.y + height * t,
                    base.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    private static void burst(ServerLevel level, Vec3 at, ParticleOptions particle, int count, double spread) {
        level.sendParticles(particle, at.x, at.y, at.z, count, spread, spread * 0.7, spread, 0.04);
    }

    private static void skyBeam(ServerLevel level, ServerPlayer player, ParticleOptions particle, double height) {
        for (int i = 0; i < 60; i++) {
            double y = player.getY() + 0.2 + i * height / 60;
            double angle = i * 0.42;
            level.sendParticles(particle, player.getX() + Math.cos(angle) * 0.4, y,
                    player.getZ() + Math.sin(angle) * 0.4, 1, 0, 0, 0, 0);
        }
    }
}
