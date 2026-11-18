package com.king.botanicfilters.compat;

import com.king.botanicfilters.BotanicFilters;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Bridges Create's filter items onto Botania's item-stack request matching.
 */
public final class FilterMatch {
	private static final ThreadLocal<Level> REQUEST_LEVEL = new ThreadLocal<>();
	private static boolean failureLogged;

	private FilterMatch() {
	}

	public static boolean isFilter(@Nullable ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof FilterItem;
	}

	/**
	 * Create attributes can need a level (item groups call into world.enabledFeatures()), so the
	 * level of the running corporea request is carried on the server thread.
	 */
	public static void bindLevel(@Nullable Level level) {
		if (level == null) {
			REQUEST_LEVEL.remove();
		} else {
			REQUEST_LEVEL.set(level);
		}
	}

	public static void clearLevel() {
		REQUEST_LEVEL.remove();
	}

	@Nullable
	public static Level currentLevel() {
		return REQUEST_LEVEL.get();
	}

	public static boolean matches(@Nullable Level boundLevel, ItemStack filterStack, ItemStack stack) {
		if (stack.isEmpty() || !(filterStack.getItem() instanceof FilterItem filterItem)) {
			return false;
		}

		Level level = boundLevel != null ? boundLevel : fallbackLevel();
		if (level == null) {
			return false;
		}

		try {
			// makeStackWrapper rather than FilterItemStack.of: the latter falls back to a plain item
			// comparison while a freshly crafted filter still has an empty component patch.
			FilterItemStack filter = filterItem.makeStackWrapper(filterStack);
			return filter.test(level, stack, false);
		} catch (RuntimeException | LinkageError e) {
			if (!failureLogged) {
				failureLogged = true;
				BotanicFilters.LOGGER.error("Could not evaluate a Create filter during a Corporea request; treating it as no match", e);
			}
			return false;
		}
	}

	@Nullable
	private static Level fallbackLevel() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? null : server.overworld();
	}
}
