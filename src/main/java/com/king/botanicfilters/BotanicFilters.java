package com.king.botanicfilters;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BotanicFilters.MOD_ID)
public final class BotanicFilters {
	public static final String MOD_ID = "botanicfilters";
	public static final Logger LOGGER = LoggerFactory.getLogger("Botanic Filters");

	public BotanicFilters() {
		LOGGER.info("Botanic Filters loaded: Create filters are honoured by Corporea Funnels");
	}
}
