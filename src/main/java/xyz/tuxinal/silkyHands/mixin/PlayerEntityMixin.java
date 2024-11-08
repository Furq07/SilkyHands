package xyz.tuxinal.silkyHands.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.tuxinal.silkyHands.utils.ConfigParser;

@Mixin(Player.class)
public class PlayerEntityMixin {
    @Inject(method = "hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("RETURN"), cancellable = true)
    private void injected(BlockState blockState, CallbackInfoReturnable<Boolean> ci) {
        Player player = ((Player) (Object) this);
        if (!player.getTags().contains(ConfigParser.getTag()) || 
            !player.getMainHandItem().isEmpty() || 
            ArrayUtils.contains(ConfigParser.getIgnoredBlocks(), BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
            return;
        }
        ci.setReturnValue(true);
    }
}