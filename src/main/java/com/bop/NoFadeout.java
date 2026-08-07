package com.bop;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "NoFadeout",
	description = "Removes some of the fadeout effects ingame"
)
public class NoFadeout extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event) {
		if (event.getScriptId() == 948) {
			event.getScriptEvent().getArguments()[4] = 255; // transparency
			event.getScriptEvent().getArguments()[5] = 0; // "fadeout" duration
		}
		if (event.getScriptId() == 952) {
			event.getScriptEvent().getArguments()[1] = client.getTickCount();
			// Script is client.getTickCount() - getArgs[1], to find the duration
			// This sets duration to 0
		}
	}
}
