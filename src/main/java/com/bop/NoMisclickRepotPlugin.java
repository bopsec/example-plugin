package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "No Misclick Repot",
	description = "Deprioritize repot options while matching buffs are still active",
	tags = {"potion", "repot", "inventory", "menu"}
)
public class NoMisclickRepotPlugin extends Plugin
{
	private static final String DRINK_OPTION = "Drink";
	private static final String EAT_OPTION = "Eat";
	private static final String NO_OP_OPTION = "Waiting...";

	@Inject
	private Client client;

	@Inject
	private NoMisclickRepotConfig config;
	/*
	Divines
	 */
	int divineBastion = VarbitID.DIVINEBASTION_POTION_TIME; // This uses DIVINERANGING and DIVINESUPERDEF as well
	int divineRanging = VarbitID.DIVINERANGE_POTION_TIME;
	List<Integer> divineBastionPots = List.of(ItemID._4DOSEDIVINEBASTION, ItemID._3DOSEDIVINEBASTION, ItemID._2DOSEDIVINEBASTION, ItemID._1DOSEDIVINEBASTION);
	List<Integer> rangingPotionPots = Stream.concat(
		divineBastionPots.stream(),
		Stream.of(
			ItemID._4DOSEDIVINERANGE,
			ItemID._3DOSEDIVINERANGE,
			ItemID._2DOSEDIVINERANGE,
			ItemID._1DOSEDIVINERANGE
		)
	).collect(Collectors.toList());
	int divineScb = VarbitID.DIVINECOMBAT_POTION_TIME;
	int divineStr = VarbitID.DIVINESTRENGTH_POTION_TIME;
	int divineAtk = VarbitID.DIVINEATTACK_POTION_TIME;
	List<Integer> divineScbPots = List.of(ItemID._4DOSEDIVINECOMBAT, ItemID._3DOSEDIVINECOMBAT, ItemID._2DOSEDIVINECOMBAT, ItemID._1DOSEDIVINECOMBAT);
	List<Integer> divineStrPots = List.of(ItemID._4DOSEDIVINESTRENGTH, ItemID._3DOSEDIVINESTRENGTH, ItemID._2DOSEDIVINESTRENGTH, ItemID._1DOSEDIVINESTRENGTH);
	List<Integer> divineAtkPots = List.of(ItemID._4DOSEDIVINEATTACK, ItemID._3DOSEDIVINEATTACK, ItemID._2DOSEDIVINEATTACK, ItemID._1DOSEDIVINEATTACK);

	/*
	Prayer regeneration potion
	 */
	int prayerRegen = VarbitID.PRAYER_REGENERATION_POTION_TIMER;
	List<Integer> prayerRegenPots = List.of(ItemID._4DOSE1PRAYER_REGENERATION, ItemID._3DOSE1PRAYER_REGENERATION, ItemID._2DOSE1PRAYER_REGENERATION, ItemID._1DOSE1PRAYER_REGENERATION);
	int coxPrayerEnhanceRate = VarbitID.RAIDS_PRAYERENHANCE_RATE;

	/*
	Antipoison / Anvi-venom
	 */
	int anti = VarPlayerID.POISON; // Negative values indicate active poison or venom immunity.
	List<Integer> venomPots = List.of(
		ItemID.ANTIVENOM_4, ItemID.ANTIVENOM_3, ItemID.ANTIVENOM_2, ItemID.ANTIVENOM_1,
		ItemID.EXTENDED_ANTIVENOM_4, ItemID.EXTENDED_ANTIVENOM_3, ItemID.EXTENDED_ANTIVENOM_2, ItemID.EXTENDED_ANTIVENOM_1,
		ItemID.ANTIVENOM1, ItemID.ANTIVENOM2, ItemID.ANTIVENOM3, ItemID.ANTIVENOM4
	);
	List<Integer> poisonPots = Stream.concat(
		venomPots.stream(),
		Stream.of(
			ItemID._1DOSEANTIPOISON, ItemID._2DOSEANTIPOISON, ItemID._3DOSEANTIPOISON, ItemID._4DOSEANTIPOISON,
			ItemID._1DOSE2ANTIPOISON, ItemID._2DOSE2ANTIPOISON, ItemID._3DOSE2ANTIPOISON, ItemID._4DOSE2ANTIPOISON,
			ItemID.ARAXYTE_VENOM_SACK
		)
	).collect(Collectors.toList());

	/*
	Raids potions
	 */
	int coxOverload = VarbitID.RAIDS_OVERLOAD_TIMER;
	List<Integer> coxOverloadPots = List.of(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_4, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_3, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_2, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_1);

	int coxEnhance = VarbitID.RAIDS_PRAYERENHANCE_TIMER;
	List<Integer> coxEnhancePots = List.of(ItemID.RAIDS_VIAL_PRAYER_STRONG_4, ItemID.RAIDS_VIAL_PRAYER_STRONG_3, ItemID.RAIDS_VIAL_PRAYER_STRONG_2, ItemID.RAIDS_VIAL_PRAYER_STRONG_1);

	int toaOverload = VarbitID.TOA_MIDRAIDLOOT_STATS_TIMER;
	List<Integer> toaOverloadPots = List.of(ItemID.TOA_SUPPLY_STATS_2, ItemID.TOA_SUPPLY_STATS_1);
	int toaLiquidAdrenaline = VarbitID.TOA_MIDRAIDLOOT_ENERGY_ACTIVE;
	List<Integer> toaLiquidAdrenalinePots = List.of(ItemID.TOA_SUPPLY_ENERGY_2, ItemID.TOA_SUPPLY_ENERGY_1);

	@Provides
	NoMisclickRepotConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NoMisclickRepotConfig.class);
	}

	@Subscribe
	public void onPostMenuSort(PostMenuSort e)
	{
		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		for (MenuEntry menuEntry : menuEntries)
		{
			if (!shouldDeprioritize(menuEntry))
			{
				continue;
			}

			client.getMenu().setMenuEntries(insertNoOpEntry(menuEntries));
			return;
		}

		client.getMenu().setMenuEntries(menuEntries);
	}

	@Override
	protected void startUp()
	{
	}

	@Override
	protected void shutDown()
	{
	}

	private boolean shouldDeprioritize(MenuEntry menuEntry)
	{
		String option = menuEntry.getOption();
		if (!DRINK_OPTION.equals(option) && !EAT_OPTION.equals(option))
		{
			return false;
		}

		int itemId = menuEntry.getItemId();
		return isActiveDivine(itemId)
			|| isActivePrayerRegen(itemId)
			|| isProtectedAntipoison(itemId)
			|| isUnderCoxPotionEffect(itemId)
			|| isActiveToaPotion(itemId);
	}

	private boolean isActiveDivine(int itemId)
	{
		if (!config.divines())
		{
			return false;
		}

		return (isActiveTimedEffect(itemId, rangingPotionPots, divineBastion) && isRangeBoostAboveThreshold())
			|| (isActiveTimedEffect(itemId, rangingPotionPots, divineRanging) && isRangeBoostAboveThreshold())
			|| (isActiveTimedEffect(itemId, divineScbPots, divineScb) && isScbBoostAboveThreshold())
			|| isActiveTimedEffect(itemId, divineStrPots, divineStr)
			|| isActiveTimedEffect(itemId, divineAtkPots, divineAtk);
	}

	private boolean isActivePrayerRegen(int itemId)
	{
		return isActivePrayerRegenEffect(itemId);
	}

	private boolean isProtectedAntipoison(int itemId)
	{
		return config.antipoison() && isActivePoisonEffect(itemId);
	}

	private boolean isUnderCoxPotionEffect(int itemId)
	{
		return isActiveCoxOverload(itemId)
			|| (config.coxEnhance() && isActiveCoxPrayerEnhanceEffect(itemId));
	}

	private boolean isActiveToaPotion(int itemId)
	{
		return (config.toaSalt() && isActiveToaSaltEffect(itemId))
			|| (config.toaLiquidAdren() && isActiveTimedEffect(itemId, toaLiquidAdrenalinePots, toaLiquidAdrenaline));
	}

	private boolean isActiveTimedEffect(int itemId, List<Integer> itemIds, int varbitId)
	{
		return itemIds.contains(itemId) && client.getVarbitValue(varbitId) > config.timeLeft();
	}

	private boolean isActivePrayerRegenEffect(int itemId)
	{
		int prayerRegenValue = client.getVarbitValue(prayerRegen);
		int remainingTicks = prayerRegenValue * 12;

		return prayerRegenPots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isActivePoisonEffect(int itemId)
	{
		int poisonValue = client.getVarpValue(anti);
		if (poisonValue > 0)
		{
			return false;
		}

		int remainingTicks = -poisonValue * 30;
		return isProtectedPoisonPotion(itemId, poisonValue) && remainingTicks > config.timeLeft();
	}

	private boolean isProtectedPoisonPotion(int itemId, int poisonValue)
	{
		if (venomPots.contains(itemId))
		{
			return poisonValue < -38;
		}

		return poisonPots.contains(itemId) && poisonValue < 0;
	}

	private boolean isActiveCoxOverload(int itemId)
	{
		return coxOverloadPots.contains(itemId) && client.getVarbitValue(coxOverload) > 0;
	}

	private boolean isActiveCoxPrayerEnhanceEffect(int itemId)
	{
		int remainingTicks = client.getVarbitValue(coxEnhance) * client.getVarbitValue(coxPrayerEnhanceRate);
		return coxEnhancePots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isActiveToaSaltEffect(int itemId)
	{
		int remainingTicks = client.getVarbitValue(toaOverload) * 25;
		return toaOverloadPots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private MenuEntry[] insertNoOpEntry(MenuEntry[] menuEntries)
	{
		MenuEntry[] newEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);
		System.arraycopy(menuEntries, 0, newEntries, 0, menuEntries.length);

		newEntries[newEntries.length - 1] = client.getMenu().createMenuEntry(newEntries.length - 1)
			.setOption(NO_OP_OPTION)
			.setTarget("")
			.setType(MenuAction.RUNELITE)
			.setForceLeftClick(true)
			.onClick(entry -> { });

		return newEntries;
	}

	private boolean isRangeBoostAboveThreshold()
	{
		return client.getBoostedSkillLevel(Skill.RANGED) > config.rangeBoostThreshold();
	}

	private boolean isScbBoostAboveThreshold()
	{
		return client.getBoostedSkillLevel(Skill.STRENGTH) > config.scbBoostThreshold();
	}
}
