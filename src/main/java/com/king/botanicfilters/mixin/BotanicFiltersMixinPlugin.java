package com.king.botanicfilters.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Upstream Botania shares the "botania" mod id but lives in org.violetmoon.botania, so the mod
 * dependency alone cannot tell the two apart. Only apply the mixins when Botania Revived is present.
 */
public final class BotanicFiltersMixinPlugin implements IMixinConfigPlugin {
	private static final String REVIVED_MARKER = "velrondevs/botania/common/impl/corporea/CorporeaItemStackMatcher.class";

	private boolean botaniaRevived;

	@Override
	public void onLoad(String mixinPackage) {
		ClassLoader loader = getClass().getClassLoader();
		botaniaRevived = loader.getResource(REVIVED_MARKER) != null;
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return botaniaRevived;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
