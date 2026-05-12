/*
 * Copyright (c) 2018, DennisDeV <https://github.com/DevDennis>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// This was certainly derived from the original RuneLite antidrag, so I'm including DennisDeV's original copyright notice.

package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ScriptID;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Anti Drag Blacklist",
	description = "Exclude configured items from RuneLite Anti Drag's custom delay",
	tags = {"antidrag", "delay", "inventory", "items", "blacklist"}
)
public class AntidragBlacklistPlugin extends Plugin
{
	static final String CONFIG_GROUP = "antidragblacklist";
	private static final String RUNELITE_ANTI_DRAG_CONFIG_GROUP = "antiDrag";
	private static final int DEFAULT_DRAG_DELAY = 0;
	private static final float AFTER_ANTI_DRAG = -1.0f;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AntidragBlacklistConfig config;

	private List<Pattern> blacklistedItemNamePatterns = Collections.emptyList();

	@Provides
	AntidragBlacklistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AntidragBlacklistConfig.class);
	}

	@Override
	protected void startUp()
	{
		reloadBlacklist();
		clientThread.invoke(this::startReapply);
	}

	@Override
	protected void shutDown()
	{
		blacklistedItemNamePatterns = Collections.emptyList();
	}

	@Subscribe(priority = AFTER_ANTI_DRAG)
	public void onConfigChanged(ConfigChanged event)
	{
		boolean blacklistConfigChanged = CONFIG_GROUP.equals(event.getGroup());
		boolean antiDragConfigChanged = RUNELITE_ANTI_DRAG_CONFIG_GROUP.equals(event.getGroup());
		if (!blacklistConfigChanged && !antiDragConfigChanged)
		{
			return;
		}
		if (blacklistConfigChanged)
		{
			reloadBlacklist();
		}
		clientThread.invoke(this::startReapply);
	}

	@Subscribe(priority = AFTER_ANTI_DRAG)
	public void onFocusChanged(FocusChanged event)
	{
		if (event.isFocused())
		{
			clientThread.invoke(this::startReapply);
		}
	}

	@Subscribe(priority = AFTER_ANTI_DRAG)
	public void onWidgetLoaded(WidgetLoaded event)
	{
		switch (event.getGroupId())
		{
			case InterfaceID.BANKMAIN:
			case InterfaceID.BANKSIDE:
			case InterfaceID.SHARED_BANK:
			case InterfaceID.SHARED_BANK_SIDE:
				applyBlacklistToBankContainers();
				break;
			case InterfaceID.INVENTORY:
				applyBlacklistToInventoryContainers();
				break;
			default:
				break;
		}
	}

	@Subscribe(priority = AFTER_ANTI_DRAG)
	public void onScriptPostFired(ScriptPostFired event)
	{
		switch (event.getScriptId())
		{
			case ScriptID.INVENTORY_DRAWITEM:
			case ScriptID.INTERFACE_INV_DRAW_SLOT_BIG:
				applyBlacklistToScriptActiveWidget();
				break;
			case ScriptID.RAIDS_STORAGE_PRIVATE_ITEMS:
				applyBlacklist(client.getWidget(InterfaceID.RaidsStoragePrivate.ITEMS));
				break;
			case ScriptID.BANK_DEPOSITBOX_INIT:
				applyBlacklistToBankContainers();
				break;
			case ScriptID.SEED_VAULT_BUILD:
				applyBlacklistToSeedVault();
				break;
			default:
				break;
		}
	}

	private void applyBlacklistToScriptActiveWidget()
	{
		Widget widget = client.getScriptActiveWidget();
		if (isBlacklistedItem(widget))
		{
			resetDragDelay(widget);
		}
	}

	private void applyBlacklistToKnownContainers()
	{
		applyBlacklistToInventoryContainers();
		applyBlacklistToBankContainers();
		applyBlacklistToSeedVault();
	}

	private void applyBlacklistToInventoryContainers()
	{
		applyBlacklist(client.getWidget(InterfaceID.Inventory.ITEMS));
		applyBlacklist(client.getWidget(InterfaceID.EquipmentSide.ITEMS));
	}

	private void applyBlacklistToBankContainers()
	{
		applyBlacklist(client.getWidget(InterfaceID.Bankmain.ITEMS));
		applyBlacklist(client.getWidget(InterfaceID.Bankside.ITEMS));
		applyBlacklist(client.getWidget(InterfaceID.SharedBank.ITEMS));
		applyBlacklist(client.getWidget(InterfaceID.SharedBankSide.ITEMS));
		applyBlacklist(client.getWidget(InterfaceID.Bankside.WORNOPS));
		applyBlacklist(client.getWidget(InterfaceID.BankDepositbox.INVENTORY));
		applyBlacklist(client.getWidget(InterfaceID.RaidsStoragePrivate.ITEMS));
	}

	private void applyBlacklistToSeedVault()
	{
		applyBlacklist(client.getWidget(InterfaceID.SeedVault.OBJ_LIST));
		applyBlacklist(client.getWidget(InterfaceID.SeedVault.TEXT_LIST));
	}

	private void applyBlacklist(Widget container)
	{
		if (container == null || isBlacklistEmpty())
		{
			return;
		}

		Widget[] children = container.getDynamicChildren();
		if (children == null)
		{
			return;
		}

		for (Widget item : children)
		{
			if (isBlacklistedItem(item))
			{
				resetDragDelay(item);
			}
		}
	}

	private void startReapply()
	{
		applyBlacklistToKnownContainers();
	}

	private static void resetDragDelay(Widget widget)
	{
		widget.setOnMouseRepeatListener((Object[]) null);
		widget.setDragDeadTime(DEFAULT_DRAG_DELAY);
	}

	private boolean isBlacklistedItem(Widget widget)
	{
		if (widget == null)
		{
			return false;
		}

		int itemId = widget.getItemId();

		if (blacklistedItemNamePatterns.isEmpty() || itemId <= 0)
		{
			return false;
		}

		ItemComposition itemComposition = client.getItemDefinition(itemId);
		String itemName = itemComposition == null ? null : itemComposition.getName();
		if (itemName == null || itemName.isEmpty())
		{
			return false;
		}

		for (Pattern pattern : blacklistedItemNamePatterns)
		{
			if (pattern.matcher(itemName).matches())
			{
				return true;
			}
		}

		return false;
	}

	private void reloadBlacklist()
	{
		blacklistedItemNamePatterns = parseItemNamePatterns(config.itemNames());
	}

	private boolean isBlacklistEmpty()
	{
		return blacklistedItemNamePatterns.isEmpty();
	}

	private static List<Pattern> parseItemNamePatterns(String rawItemNames)
	{
		if (rawItemNames == null || rawItemNames.trim().isEmpty())
		{
			return Collections.emptyList();
		}

		List<Pattern> patterns = new ArrayList<>();
		for (String token : rawItemNames.split("[,\\r\\n]+"))
		{
			String pattern = token.trim();
			if (!pattern.isEmpty())
			{
				patterns.add(Pattern.compile(toWildcardRegex(pattern), Pattern.CASE_INSENSITIVE));
			}
		}

		return patterns;
	}

	private static String toWildcardRegex(String wildcard)
	{
		StringBuilder regex = new StringBuilder("^");
		for (int i = 0; i < wildcard.length(); i++)
		{
			char c = wildcard.charAt(i);
			if (c == '*')
			{
				regex.append(".*");
			}
			else if (c == '?')
			{
				regex.append('.');
			}
			else
			{
				regex.append(Pattern.quote(String.valueOf(c)));
			}
		}
		regex.append('$');
		return regex.toString();
	}
}
