package com.king.botanicfilters.mixin;

import com.king.botanicfilters.compat.FilterMatch;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velrondevs.botania.common.impl.corporea.CorporeaItemStackMatcher;

/**
 * A Create filter sitting in an item frame on a Corporea Funnel ends up as the stored stack of the
 * funnel's request matcher. Match against the filter instead of that single item.
 */
@Pseudo
@Mixin(value = CorporeaItemStackMatcher.class, remap = false)
public class CorporeaItemStackMatcherMixin {
	@Shadow
	@Final
	private ItemStack match;

	@Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	private void botanicfilters$matchCreateFilter(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!stack.isEmpty() && FilterMatch.isFilter(match)) {
			cir.setReturnValue(FilterMatch.matches(FilterMatch.currentLevel(), match, stack));
		}
	}
}
