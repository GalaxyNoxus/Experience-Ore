package dev.galaxynoxus.xpore;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractExperienceOreBlock extends Block {
    protected AbstractExperienceOreBlock(Settings settings) {
        super(settings);

    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos,
                           BlockState state, @Nullable BlockEntity blockEntity,
                           ItemStack tool) {

        if (!(world instanceof ServerWorld serverWorld)
                || player.isCreative() || !tool.isSuitableFor(state)) {
            return;
        }

        super.afterBreak(world, player, pos, state, blockEntity, tool);

        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.35, 0.35, 0.35, 0.05);

        if (!serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            return;
        }

        int silkTouch = EnchantmentHelper.getLevel(
                Enchantments.SILK_TOUCH, tool);

        if (silkTouch > 0) {

            dropStack(serverWorld, pos, new ItemStack(this));
            return;
        }

        int fortune = EnchantmentHelper.getLevel(
                Enchantments.FORTUNE, tool);
        XpOreConfig config = XpOreConfig.get();
        int baseXp = config.minXpDrop() + serverWorld.random.nextInt(
                config.maxXpDrop() - config.minXpDrop() + 1);
        int totalXp = config.applyFortune(baseXp, fortune);

        if (totalXp > 0) { ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(pos), totalXp); }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isClient) { return; }
        boolean exposed = false;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.offset(direction);
            if (!world.getBlockState(neighbor).isOpaqueFullCube(world, neighbor)) {
                exposed = true;
                break;
            }
        }
        if (!exposed) { return; }
        for (int slot = 0; slot < OrbitMath.COUNT; slot++) {
            world.addParticle(ModParticles.XP_AURA,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    slot, 0.0, 0.0);
        }
    }
}
