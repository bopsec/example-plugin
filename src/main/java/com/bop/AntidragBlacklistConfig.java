package com.bop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(AntidragBlacklistPlugin.CONFIG_GROUP)
public interface AntidragBlacklistConfig extends Config
{
	@ConfigItem(
		keyName = "itemNames",
		name = "Item names",
		description = "Comma or newline separated item name patterns. Supports * and ? wildcards.",
		position = 1
	)
	default String itemNames()
	{
		return "";
	}
}
