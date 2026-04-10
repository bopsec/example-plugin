package com.bop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("NoMisclickRepot")
public interface NoMisclickRepotConfig extends Config
{
	@ConfigItem(
		keyName = "timeLeft",
		name = "Repot time",
		description = "The time, in ticks, left before potion effect wears off, before we allow repotting",
		position = 0
	)
	default int timeLeft()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "divines",
		name = "Divines",
		description = "Enable for divines",
		position = 1
	)
	default boolean divines()
	{
		return true;
	}
	@ConfigItem(
		keyName = "rangeBoostThreshold",
		name = "Range boost threshold",
		description = "Threshold under which we ignore the repot time",
		position = 2
	)
	default int rangeBoostThreshold()
	{
		return 108;
	}

	@ConfigItem(
		keyName = "scbBoostThreshold",
		name = "Scb boost threshold",
		description = "Threshold under which we ignore the repot time, specifically checks Strength boost",
		position = 3
	)
	default int scbBoostThreshold()
	{
		return 114;
	}

	@ConfigItem(
		keyName = "antipoison",
		name = "Antipoison/venom",
		description = "Enable for antipoisons and antivenoms",
		position = 4
	)
	default boolean antipoison()
	{
		return true;
	}

	@ConfigItem(
		keyName = "coxEnhance",
		name = "CoX Enhance",
		description = "Enable for CoX Prayer Enhance",
		position = 10
	)
	default boolean coxEnhance()
	{
		return false;
	}

	@ConfigItem(
		keyName = "toaSalt",
		name = "Toa Salt",
		description = "Enable for TOA salt",
		position = 20
	)
	default boolean toaSalt()
	{
		return true;
	}

	@ConfigItem(
		keyName = "toaLiquidAdren",
		name = "Toa Adren",
		description = "Enable for TOA Liquid Adrenaline",
		position = 21
	)
	default boolean toaLiquidAdren()
	{
		return true;
	}
}
