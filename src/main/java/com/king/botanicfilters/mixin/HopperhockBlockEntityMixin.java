package com.king.botanicfilters.mixin;

import com.king.botanicfilters.compat.FilterMatch;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velrondevs.botania.common.block.flower.functional.HopperhockBlockEntity;
import velrondevs.botania.common.helper.ItemNBTHelper;

import java.util.List;

/**
 * A Hopperhock reads its filters out of item frames and compares each one with
 * {@link ItemNBTHelper#matchTagAndManaFullness}, so a Create filter there only ever matches the filter
 * item itself. Take over the comparison whenever such a filter is present; every other setup keeps
 * Botania's own behaviour because the handler bails out before cancelling.
 */
@Pseudo
@Mixin(value = HopperhockBlockEntity.class, remap = false)
public class HopperhockBlockEntityMixin {
	private static final int BLACKLIST = 1;

	@Inject(method = "canAcceptItem(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;I)Z", at = @At("HEAD"),
			cancellable = true, remap = false)
	private static void botanicfilters$matchCreateFilter(ItemStack stack, List<ItemStack> filters, int filterType,
			CallbackInfoReturnable<Boolean> cir) {
		// Botania answers the empty cases before it ever looks at a filter; leave those to it.
		if (stack.isEmpty() || filters.isEmpty()) {
			return;
		}

		boolean hasCreateFilter = false;
		for (ItemStack filter : filters) {
			if (FilterMatch.isFilter(filter)) {
				hasCreateFilter = true;
				break;
			}
		}

		if (!hasCreateFilter) {
			return;
		}

		// Botania's blacklist mode is exactly the negation of its whitelist mode.
		boolean accepted = botanicfilters$accepts(stack, filters);
		cir.setReturnValue(filterType == BLACKLIST ? !accepted : accepted);
	}

	/**
	 * Mirrors Botania's whitelist pass: any non-empty filter counts as "there is a filter", the first
	 * match accepts the stack, and an empty filter list of real entries accepts everything.
	 */
	private static boolean botanicfilters$accepts(ItemStack stack, List<ItemStack> filters) {
		boolean hasFilter = false;
		for (ItemStack filter : filters) {
			if (filter == null || filter.isEmpty()) {
				continue;
			}

			hasFilter = true;
			boolean matches = FilterMatch.isFilter(filter)
					? FilterMatch.matches(FilterMatch.currentLevel(), filter, stack)
					: ItemNBTHelper.matchTagAndManaFullness(stack, filter);
			if (matches) {
				return true;
			}
		}

		return !hasFilter;
	}
}
