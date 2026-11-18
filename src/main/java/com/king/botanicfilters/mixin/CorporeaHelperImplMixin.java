package com.king.botanicfilters.mixin;

import com.king.botanicfilters.compat.FilterMatch;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import velrondevs.botania.api.corporea.CorporeaNode;
import velrondevs.botania.api.corporea.CorporeaRequestMatcher;
import velrondevs.botania.api.corporea.CorporeaResult;
import velrondevs.botania.api.corporea.CorporeaSpark;
import velrondevs.botania.common.impl.corporea.CorporeaHelperImpl;
import velrondevs.botania.common.impl.corporea.CorporeaResultImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Corporea request matchers have no level argument, but Create's attribute filters can need one.
 * Bind the level of the running request for the duration of the matcher walk.
 */
@Pseudo
@Mixin(value = CorporeaHelperImpl.class, remap = false)
public class CorporeaHelperImplMixin {
	private static final String REQUEST_ITEM = "requestItem(Lvelrondevs/botania/api/corporea/CorporeaRequestMatcher;ILvelrondevs/botania/api/corporea/CorporeaSpark;Lnet/minecraft/world/entity/LivingEntity;Z)Lvelrondevs/botania/api/corporea/CorporeaResult;";

	@Inject(method = REQUEST_ITEM, at = @At("HEAD"), remap = false)
	private void botanicfilters$bindRequestLevel(CorporeaRequestMatcher matcher, int count, CorporeaSpark spark,
			@Nullable LivingEntity requestor, boolean doit, CallbackInfoReturnable<CorporeaResult> cir) {
		FilterMatch.bindLevel(resolveLevel(spark, requestor));
	}

	@Inject(method = REQUEST_ITEM, at = @At("RETURN"), remap = false)
	private void botanicfilters$releaseRequestLevel(CorporeaRequestMatcher matcher, int count, CorporeaSpark spark,
			@Nullable LivingEntity requestor, boolean doit, CallbackInfoReturnable<CorporeaResult> cir) {
		FilterMatch.clearLevel();
	}

	/**
	 * Botania assumes a node that reports a matching stack also gives it up. Inventories that only
	 * expose a transient item (Create's Packager returns an empty stack while it animates, but still
	 * reports its held box) end up as an {@link ItemStack#EMPTY} in the result, and the corporea
	 * spark crashes building item particles from it.
	 */
	@ModifyReturnValue(method = REQUEST_ITEM, at = @At("RETURN"), remap = false)
	private static CorporeaResult botanicfilters$dropEmptyStacks(CorporeaResult result) {
		if (result == null) {
			return null;
		}

		List<ItemStack> stacks = result.stacks();
		if (stacks == null) {
			return result;
		}

		boolean hasEmpty = false;
		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) {
				hasEmpty = true;
				break;
			}
		}

		if (!hasEmpty) {
			return result;
		}

		List<ItemStack> cleaned = new ArrayList<>(stacks.size());
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				cleaned.add(stack);
			}
		}

		return new CorporeaResultImpl(cleaned, result.matchedCount(), result.extractedCount(),
				result.matchCountsByNode());
	}

	@Nullable
	private static Level resolveLevel(@Nullable CorporeaSpark spark, @Nullable LivingEntity requestor) {
		if (spark != null) {
			Entity entity = spark.entity();
			if (entity != null) {
				return entity.level();
			}
			CorporeaNode node = spark.getSparkNode();
			if (node != null) {
				return node.getWorld();
			}
		}
		return requestor == null ? null : requestor.level();
	}
}
