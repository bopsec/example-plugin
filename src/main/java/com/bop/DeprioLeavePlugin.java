package com.bop;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Deprio Leave",
	description = "Deprioritize the leave option when configured loot is on the ground"
)
public class DeprioLeavePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private DeprioLeaveConfig config;

	private List<Pattern> deprioOptionPatterns = List.of();

	private final Map<WorldPoint, Map<Integer, Integer>> groundItems = new HashMap<>();

	private List<Pattern> whitelistPatterns = List.of();

	@Override
	protected void startUp() throws Exception
	{
		reloadWhitelist();
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOADING)
		{
			groundItems.clear();
		}
	}


	@Provides
	DeprioLeaveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DeprioLeaveConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals("DeprioLeave"))
			reloadWhitelist();
	}


	@Subscribe(priority = -1)
	public void onPostMenuSort(PostMenuSort e)
	{
		if (!lootExists() || !client.getTopLevelWorldView().isInstance())
			return;

		MenuEntry[] entries = client.getMenuEntries();

		int walkIdx = -1;
		for (int i = 0; i < entries.length; i++)
			if (entries[i].getType() == MenuAction.WALK)
				walkIdx = i;

		if (walkIdx == -1)
			return;

		List<MenuEntry> exits = new ArrayList<>();
		List<MenuEntry> rest = new ArrayList<>();

		for (MenuEntry me : entries)
		{
			if (isExitOption(me))
				exits.add(me);
			else
				rest.add(me);
		}

		int insertPos = rest.size();
		for (int i = 0; i < rest.size(); i++)
			if (rest.get(i).getType() == MenuAction.WALK)
				insertPos = i;

		rest.addAll(insertPos, exits);

		client.setMenuEntries(rest.toArray(new MenuEntry[0]));
	}




	private boolean lootExists()
	{
		int threshold = config.lootValue();

		for (Map<Integer, Integer> tile : groundItems.values())
		{
			for (var entry : tile.entrySet())
			{
				int id = entry.getKey();
				int qty = entry.getValue();

				if (isWhitelisted(id))
					return true;

				long value = (long) itemManager.getItemPrice(id) * qty;
				if (value >= threshold)
					return true;
			}
		}
		return false;
	}


	@Subscribe
	public void onItemSpawned(ItemSpawned e)
	{
		WorldPoint wp = e.getTile().getWorldLocation();
		TileItem item = e.getItem();

		groundItems
				.computeIfAbsent(wp, k -> new HashMap<>())
				.merge(item.getId(), item.getQuantity(), Integer::sum);
		rebuildMenu();
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned e)
	{
		WorldPoint wp = e.getTile().getWorldLocation();
		Map<Integer, Integer> tile = groundItems.get(wp);
		if (tile == null)
			return;

		tile.remove(e.getItem().getId());

		if (tile.isEmpty())
			groundItems.remove(wp);

		rebuildMenu();
	}

	@Subscribe
	public void onItemQuantityChanged(ItemQuantityChanged e)
	{
		WorldPoint wp = e.getTile().getWorldLocation();
		groundItems
				.computeIfAbsent(wp, k -> new HashMap<>())
				.put(e.getItem().getId(), e.getNewQuantity());

		rebuildMenu();
	}



	private boolean isExitOption(MenuEntry e)
	{
		String opt = Text.removeTags(e.getOption()).toLowerCase();

		for (Pattern p : deprioOptionPatterns)
			if (p.matcher(opt).matches())
				return true;

		return false;
	}

	private boolean isWhitelisted(int itemId)
	{
		String name = itemManager.getItemComposition(itemId).getName().toLowerCase();

		for (Pattern p : whitelistPatterns)
			if (p.matcher(name).matches())
				return true;

		return false;
	}

	private void rebuildMenu()
	{
		if (client.isMenuOpen())
		{
			client.setMenuEntries(client.getMenuEntries());
		}
	}

	private void reloadWhitelist()
	{
		whitelistPatterns = Arrays.stream(config.whitelist().split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(this::wildcardToRegex)
				.map(Pattern::compile)
				.collect(Collectors.toList());
		// and menuoptions to deprio
		// can we add stuff like "teleport" / "break" ? idk
		deprioOptionPatterns = Arrays.stream(config.deprioOptions().split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(String::toLowerCase)
				.map(this::wildcardToRegex)
				.map(Pattern::compile)
				.collect(Collectors.toList());
	}

	private String wildcardToRegex(String s)
	{
		StringBuilder out = new StringBuilder("^");
		for (char c : s.toLowerCase().toCharArray())
		{
			if (c == '*') out.append(".*");
			else out.append(Pattern.quote(String.valueOf(c)));
		}
		out.append("$");
		return out.toString();
	}
}
