package xyz.tuxinal.silkyHands.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.tuxinal.silkyHands.utils.ConfigParser;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {
    @Redirect(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static List<ItemStack> inject(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack) {
        if (!(entity instanceof Player player) || 
            !player.getTags().contains(ConfigParser.getTag()) ||
            !player.getMainHandItem().isEmpty()) {
            return Block.getDrops(blockState, serverLevel, blockPos, blockEntity, entity, itemStack);
        }

        Block block = blockState.getBlock();
        if (ArrayUtils.contains(ConfigParser.getIgnoredBlocks(), 
            BuiltInRegistries.BLOCK.getKey(block))) {
            return Block.getDrops(blockState, serverLevel, blockPos, blockEntity, entity, itemStack);
        }

        List<ItemStack> originalDrops = Block.getDrops(blockState, serverLevel, blockPos, blockEntity, entity, itemStack);
        if (originalDrops.stream().anyMatch(stack -> stack.getItem() == block.asItem())) {
            return originalDrops;
        }

        var lootTable = serverLevel.getServer().reloadableRegistries()
                .getLootTable(block.getLootTable());
        if (lootTable.pools.get(0).entries.get(0).conditions.stream()
                .anyMatch(condition -> condition.getType() == LootItemConditions.BLOCK_STATE_PROPERTY)) {
            return originalDrops;
        }

        return List.of(new ItemStack(block));
    }
}