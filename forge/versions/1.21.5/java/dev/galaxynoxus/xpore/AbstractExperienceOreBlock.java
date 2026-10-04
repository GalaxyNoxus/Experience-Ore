package dev.galaxynoxus.xpore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public abstract class AbstractExperienceOreBlock extends Block {
    protected AbstractExperienceOreBlock(Properties settings) {
        super(settings);

    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos,
                           BlockState state, BlockEntity blockEntity,
                           ItemStack tool) {

        if (!(world instanceof ServerLevel serverWorld)
                || player.isCreative() || !tool.isCorrectToolForDrops(state)) {
            return;
        }

        super.playerDestroy(world, player, pos, state, blockEntity, tool);

        serverWorld.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.35, 0.35, 0.35, 0.05);

        if (!serverWorld.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            return;
        }

        var enchantments = serverWorld.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(
                enchantments.getOrThrow(Enchantments.SILK_TOUCH), tool);

        if (silkTouch > 0) {

            popResource(serverWorld, pos, new ItemStack(this));
            return;
        }

        int fortune = EnchantmentHelper.getItemEnchantmentLevel(
                enchantments.getOrThrow(Enchantments.FORTUNE), tool);
        XpOreConfig config = XpOreConfig.get();
        int baseXp = config.minXpDrop() + serverWorld.random.nextInt(
                config.maxXpDrop() - config.minXpDrop() + 1);
        int totalXp = config.applyFortune(baseXp, fortune);

        if (totalXp > 0) { ExperienceOrb.award(serverWorld, Vec3.atCenterOf(pos), totalXp); }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide) { return; }
        boolean exposed = false;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (!world.getBlockState(neighbor).isSolidRender()) {
                exposed = true;
                break;
            }
        }
        if (!exposed) { return; }
        for (int slot = 0; slot < OrbitMath.COUNT; slot++) {
            world.addParticle(ModParticles.XP_AURA.get(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    slot, 0.0, 0.0);
        }
    }
}
