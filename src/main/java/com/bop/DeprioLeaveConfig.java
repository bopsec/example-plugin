package com.bop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("DeprioLeave")
public interface DeprioLeaveConfig extends Config
{
	@ConfigItem(
			keyName = "lootValue",
			name = "Value over",
			description = "Deprio leave if item/stack value over x",
			position = 0
	)
	default int lootValue() { return 10000000; }
	@ConfigItem(
			keyName = "whitelist",
			name = "Whitelist",
			description = "Comma seperated item whitelist",
			position = 1
	)
	default String whitelist() { return "*(elite), *(hard), *(master), Twisted bow"; }
	@ConfigItem(
			keyName = "deprioOptions",
			name = "Deprioritized object options",
			description = "Comma separated list of menu options to deprioritize (supports * wildcards)",
			position = 5
	)
	default String deprioOptions()
	{
		return "exit,leave,quick-exit,quick-leave,pass-through,travel,quick-travel";
	}
	@ConfigItem(
			keyName= "deprioItems",
			name = "Deprioritized widget options",
			description = "Comma separated list of menu options to deprioritize (supports * wildcards). " +
					"Works for inventory, equipment and spells",
			position = 6
	)
	default String deprioItems()
	{
		return "break,nardah";
	}
}
