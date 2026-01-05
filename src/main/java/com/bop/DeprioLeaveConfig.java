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
			description = "Deprio leave if item/stack value over x"
	)
	default int lootValue() { return 10000000; }
	@ConfigItem(
			keyName = "whitelist",
			name = "Whitelist",
			description = "Comma seperated item whitelist"
	)
	default String whitelist() { return "*(elite), *(hard), *(master), Twisted bow"; }
	@ConfigItem(
			keyName = "deprioOptions",
			name = "Deprioritized options",
			description = "Comma separated list of menu options to deprioritize (supports * wildcards)"
	)
	default String deprioOptions()
	{
		return "exit,leave,quick-exit,quick-leave";
	}
}
