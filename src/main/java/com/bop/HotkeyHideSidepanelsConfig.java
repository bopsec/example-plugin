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

	@ConfigItem(
		keyName = "respectPressEnterToChat",
		name = "Respect Press Enter to Chat",
		description = "Prevents the hotkey from firing while typing with RuneLite's Key Remapping plugin.",
		position = 1
	)
	default boolean respectPressEnterToChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "closeOnly",
		name = "Close Only",
		description = "Only closes the currently open side panel. Never opens one, so the hotkey stops acting as a toggle.",
		position = 2
	)
	default boolean closeOnly()
	{
		return false;
	}
}
