package com.bop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("HotkeyHideSidepanels")
public interface HotkeyHideSidepanelsConfig extends Config
{
	@ConfigItem(
		keyName = "hotkey",
		name = "Hotkey",
		description = "",
		position = 0
	)
	default Keybind hotkey()
	{
		return Keybind.NOT_SET;
	}
}
