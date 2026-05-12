package com.bop;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("NoMisclickRepot")
public interface NoMisclickRepotConfig extends Config
{
	@ConfigSection(
		name = "Potions",
		description = "Potion types to deprioritize while their effects are active",
		position = 1
	)
	String potionsSection = "potions";

	@ConfigSection(
		name = "Overlay",
		description = "Inventory indicators for blocked potion items",
		position = 3
	)
	String overlaySection = "overlay";

	@ConfigSection(
		name = "Advanced",
		description = "Advanced behavior overrides",
		position = 4
	)
	String advancedSection = "advanced";

	@ConfigSection(
		name = "Non-timed Potions",
		description = "Potion types to deprioritize using stat checks only",
		position = 2
	)
	String nonTimedPotionsSection = "nonTimedPotions";

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
		position = 1,
		section = potionsSection
	)
	default boolean divines()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rangeBoostThreshold",
		name = "Range boost threshold",
		description = "Threshold under which we ignore the repot time",
		position = 2,
		section = potionsSection
	)
	default int rangeBoostThreshold()
	{
		return 108;
	}

	@ConfigItem(
		keyName = "scbBoostThreshold",
		name = "Scb boost threshold",
		description = "Threshold under which we ignore the repot time, specifically checks Strength boost",
		position = 3,
		section = potionsSection
	)
	default int scbBoostThreshold()
	{
		return 114;
	}

	@ConfigItem(
		keyName = "divineHpBypassThreshold",
		name = "Divine HP bypass",
		description = "Allow divine repotting when current Hitpoints is below this value. Set to 0 to disable.",
		position = 1,
		section = advancedSection
	)
	default int divineHpBypassThreshold()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "rangingPotions",
		name = "Ranging potions",
		description = "Enable for non-divine ranging potions",
		position = 0,
		section = nonTimedPotionsSection
	)
	default boolean rangingPotions()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rangingPotionBoostThreshold",
		name = "Range boost threshold",
		description = "Block ranging potions while Ranged is above this value",
		position = 1,
		section = nonTimedPotionsSection
	)
	default int rangingPotionBoostThreshold()
	{
		return 109;
	}

	@ConfigItem(
		keyName = "superCombatPotions",
		name = "Super combat potions",
		description = "Enable for non-divine super combat potions",
		position = 2,
		section = nonTimedPotionsSection
	)
	default boolean superCombatPotions()
	{
		return false;
	}

	@ConfigItem(
		keyName = "superCombatPotionBoostThreshold",
		name = "Super combat threshold",
		description = "Block super combat potions while Strength is above this value",
		position = 3,
		section = nonTimedPotionsSection
	)
	default int superCombatPotionBoostThreshold()
	{
		return 116;
	}

	@ConfigItem(
		keyName = "antipoison",
		name = "Antipoison/venom",
		description = "Enable for antipoisons and antivenoms",
		position = 10,
		section = potionsSection
	)
	default boolean antipoison()
	{
		return true;
	}

	@ConfigItem(
		keyName = "prayerRegen",
		name = "Prayer regen",
		description = "Enable for prayer regeneration potions",
		position = 11,
		section = potionsSection
	)
	default boolean prayerRegen()
	{
		return true;
	}

	@ConfigItem(
		keyName = "goading",
		name = "Goading",
		description = "Enable for goading potions",
		position = 12,
		section = potionsSection
	)
	default boolean goading()
	{
		return true;
	}

	@ConfigItem(
		keyName = "coxOverload",
		name = "CoX Overload",
		description = "Enable for CoX overloads (only +)",
		position = 20,
		section = potionsSection
	)
	default boolean coxOverload()
	{
		return true;
	}

	@ConfigItem(
		keyName = "coxEnhance",
		name = "CoX Enhance",
		description = "Enable for CoX Prayer Enhance",
		position = 21,
		section = potionsSection
	)
	default boolean coxEnhance()
	{
		return true;
	}

	@ConfigItem(
		keyName = "toaSalt",
		name = "Toa Salt",
		description = "Enable for TOA salt",
		position = 30,
		section = potionsSection
	)
	default boolean toaSalt()
	{
		return true;
	}

	@ConfigItem(
		keyName = "toaLiquidAdren",
		name = "Toa Adren",
		description = "Enable for TOA Liquid Adrenaline",
		position = 31,
		section = potionsSection
	)
	default boolean toaLiquidAdren()
	{
		return true;
	}

	@ConfigItem(
		keyName = "blockedOverlay",
		name = "Blocked overlay",
		description = "Show an indicator on potion items while repotting is blocked",
		position = 30,
		section = overlaySection
	)
	default boolean blockedOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "repotTimer",
		name = "Repot timer",
		description = "Ticks until repotting is allowed, only updates when varb updates",
		position = 31,
		section = overlaySection
	)
	default boolean repotTimer()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "blockedOverlayColor",
		name = "Overlay colour",
		description = "Colour used for blocked potion item overlays",
		position = 32,
		section = overlaySection
	)
	default Color blockedOverlayColor()
	{
		return new Color(110, 120, 125, 80);
	}

	@ConfigItem(
		keyName = "timerTextSize",
		name = "Timer text size",
		description = "Text size for the repot timer",
		position = 33,
		section = overlaySection
	)
	default int timerTextSize()
	{
		return 14;
	}
}
