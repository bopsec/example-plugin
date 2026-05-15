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

	@ConfigItem(
		keyName = "inventoryOnly",
		name = "Inventory only",
		description = "Only apply the blacklist to inventory widgets, ignoring bank and other item containers.",
		position = 2
	)
	default boolean inventoryOnly()
	{
		return false;
	}
}
