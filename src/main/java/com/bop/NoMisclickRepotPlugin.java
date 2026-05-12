package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "No Misclick Repot",
	description = "Deprioritize Drink options while matching buffs are still active",
	tags = {"potion", "repot", "inventory", "pot", "misclick"}
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

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NoMisclickRepotItemOverlay itemOverlay;

	/*
	For timers and such, look at clientscript 5923 (buff_bar_get_value)
	https://github.com/Joshua-F/osrs-dumps/blob/master/script/%5Bproc%2Cbuff_bar_get_value%5D.cs2
	Can also see how often the varbit changes here etc.
	 */
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
	int divineScb = VarbitID.DIVINECOMBAT_POTION_TIME; // This uses ATK, DEF and STR as well
	int divineStr = VarbitID.DIVINESTRENGTH_POTION_TIME;
	int divineAtk = VarbitID.DIVINEATTACK_POTION_TIME;
	// fuck a def pot
	List<Integer> divineScbPots = List.of(ItemID._4DOSEDIVINECOMBAT, ItemID._3DOSEDIVINECOMBAT, ItemID._2DOSEDIVINECOMBAT, ItemID._1DOSEDIVINECOMBAT);
	List<Integer> divineStrPots = List.of(ItemID._4DOSEDIVINESTRENGTH, ItemID._3DOSEDIVINESTRENGTH, ItemID._2DOSEDIVINESTRENGTH, ItemID._1DOSEDIVINESTRENGTH);
	List<Integer> divineAtkPots = List.of(ItemID._4DOSEDIVINEATTACK, ItemID._3DOSEDIVINEATTACK, ItemID._2DOSEDIVINEATTACK, ItemID._1DOSEDIVINEATTACK);

	/*
	Non-timed potions
	 */
	List<Integer> regularRangingPots = List.of(ItemID._4DOSERANGERSPOTION, ItemID._3DOSERANGERSPOTION, ItemID._2DOSERANGERSPOTION, ItemID._1DOSERANGERSPOTION);
	List<Integer> regularSuperCombatPots = List.of(ItemID._4DOSE2COMBAT, ItemID._3DOSE2COMBAT, ItemID._2DOSE2COMBAT, ItemID._1DOSE2COMBAT);

	/*
	Prayer regeneration potion
	 */
	int prayerRegen = VarbitID.PRAYER_REGENERATION_POTION_TIMER;
	List<Integer> prayerRegenPots = List.of(ItemID._4DOSE1PRAYER_REGENERATION, ItemID._3DOSE1PRAYER_REGENERATION, ItemID._2DOSE1PRAYER_REGENERATION, ItemID._1DOSE1PRAYER_REGENERATION);

	/*
	Goading potion
	 */
	int goading = VarbitID.GOADING_POTION_TIMER;
	List<Integer> goadingPots = List.of(ItemID._4DOSEGOADING, ItemID._3DOSEGOADING, ItemID._2DOSEGOADING, ItemID._1DOSEGOADING);

	/*
	Antipoison / Anti-venom
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
	int coxPrayerEnhanceRate = VarbitID.RAIDS_PRAYERENHANCE_RATE;

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
		if (menuEntries.length == 0)
		{
			return;
		}

		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		if (shouldDeprioritize(topEntry))
		{
			client.getMenu().setMenuEntries(insertNoOpEntry(menuEntries));
		}
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(itemOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(itemOverlay);
	}

	private boolean shouldDeprioritize(MenuEntry menuEntry)
	{
		String option = menuEntry.getOption();
		if (!DRINK_OPTION.equals(option) && !EAT_OPTION.equals(option))
		{
			return false;
		}

		int itemId = menuEntry.getItemId();
		return getPotionOverlayState(itemId) != null;
	}

	private boolean isActiveDivine(int itemId)
	{
		if (!config.divines())
		{
			return false;
		}

		if (isBelowDivineHpBypassThreshold())
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
		return config.prayerRegen() && isActivePrayerRegenEffect(itemId);
	}

	private boolean isActiveNonTimedPotion(int itemId)
	{
		return (config.rangingPotions() && regularRangingPots.contains(itemId) && isRegularRangeBoostAboveThreshold())
			|| (config.superCombatPotions() && regularSuperCombatPots.contains(itemId) && isRegularSuperCombatBoostAboveThreshold());
	}

	private boolean isActiveGoading(int itemId)
	{
		if (!config.goading())
		{
			return false;
		}

		int goadingValue = client.getVarbitValue(goading);
		int remainingTicks = goadingValue * 6;

		return goadingPots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isProtectedAntipoison(int itemId)
	{
		return config.antipoison() && isActivePoisonEffect(itemId);
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

	private boolean isRegularRangeBoostAboveThreshold()
	{
		return client.getBoostedSkillLevel(Skill.RANGED) > config.rangingPotionBoostThreshold();
	}

	private boolean isRegularSuperCombatBoostAboveThreshold()
	{
		return client.getBoostedSkillLevel(Skill.STRENGTH) > config.superCombatPotionBoostThreshold();
	}

	private boolean isBelowDivineHpBypassThreshold()
	{
		int threshold = config.divineHpBypassThreshold();
		return threshold > 0 && client.getBoostedSkillLevel(Skill.HITPOINTS) < threshold;
	}

	PotionOverlayState getPotionOverlayState(int itemId)
	{
		if (isActiveDivine(itemId))
		{
			return new PotionOverlayState(getPotionGroup(itemId), ticksUntilAllowed(getDivineRemainingTicks(itemId)), getPotionDose(itemId));
		}

		if (isActiveNonTimedPotion(itemId))
		{
			return new PotionOverlayState(getPotionGroup(itemId), 0, getPotionDose(itemId), false);
		}

		if (isActivePrayerRegen(itemId))
		{
			return new PotionOverlayState(PotionGroup.PRAYER_REGEN, ticksUntilAllowed(client.getVarbitValue(prayerRegen) * 12), getPotionDose(itemId));
		}

		if (isActiveGoading(itemId))
		{
			return new PotionOverlayState(PotionGroup.GOADING, ticksUntilAllowed(client.getVarbitValue(goading) * 6), getPotionDose(itemId));
		}

		if (isProtectedAntipoison(itemId))
		{
			return new PotionOverlayState(getPotionGroup(itemId), ticksUntilAllowed(-client.getVarpValue(anti) * 30), getPotionDose(itemId));
		}

		if (config.coxOverload() && isActiveCoxOverload(itemId))
		{
			return new PotionOverlayState(PotionGroup.COX_OVERLOAD, client.getVarbitValue(coxOverload), getPotionDose(itemId));
		}

		if (config.coxEnhance() && isActiveCoxPrayerEnhanceEffect(itemId))
		{
			return new PotionOverlayState(PotionGroup.COX_ENHANCE, ticksUntilAllowed(client.getVarbitValue(coxEnhance) * client.getVarbitValue(coxPrayerEnhanceRate)), getPotionDose(itemId));
		}

		if (config.toaSalt() && isActiveToaSaltEffect(itemId))
		{
			return new PotionOverlayState(PotionGroup.TOA_SALT, ticksUntilAllowed(client.getVarbitValue(toaOverload) * 25), getPotionDose(itemId));
		}

		if (config.toaLiquidAdren() && isActiveTimedEffect(itemId, toaLiquidAdrenalinePots, toaLiquidAdrenaline))
		{
			return new PotionOverlayState(PotionGroup.TOA_LIQUID_ADRENALINE, ticksUntilAllowed(client.getVarbitValue(toaLiquidAdrenaline)), getPotionDose(itemId));
		}

		return null;
	}

	boolean shouldShowTimer(int itemSlot, PotionOverlayState state)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV); // 93
		if (inventory == null)
		{
			return true;
		}

		int selectedSlot = -1;
		int selectedDose = Integer.MAX_VALUE;
		Item[] items = inventory.getItems();
		for (int i = 0; i < items.length; i++)
		{
			PotionOverlayState itemState = getPotionOverlayState(items[i].getId());
			if (itemState == null || itemState.group != state.group)
			{
				continue;
			}

			if (itemState.dose < selectedDose)
			{
				selectedDose = itemState.dose;
				selectedSlot = i;
			}
		}

		return selectedSlot == -1 || selectedSlot == itemSlot;
	}

	private int getDivineRemainingTicks(int itemId)
	{
		int remainingTicks = 0;
		if (rangingPotionPots.contains(itemId) && isRangeBoostAboveThreshold())
		{
			remainingTicks = Math.max(remainingTicks, client.getVarbitValue(divineBastion));
			remainingTicks = Math.max(remainingTicks, client.getVarbitValue(divineRanging));
		}

		if (divineScbPots.contains(itemId) && isScbBoostAboveThreshold())
		{
			remainingTicks = Math.max(remainingTicks, client.getVarbitValue(divineScb));
		}

		if (divineStrPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, client.getVarbitValue(divineStr));
		}

		if (divineAtkPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, client.getVarbitValue(divineAtk));
		}

		return remainingTicks;
	}

	private int ticksUntilAllowed(int remainingTicks)
	{
		return Math.max(0, remainingTicks - config.timeLeft());
	}

	private PotionGroup getPotionGroup(int itemId)
	{
		if (divineBastionPots.contains(itemId))
		{
			return PotionGroup.DIVINE_BASTION;
		}

		if (rangingPotionPots.contains(itemId))
		{
			return PotionGroup.DIVINE_RANGING;
		}

		if (divineScbPots.contains(itemId))
		{
			return PotionGroup.DIVINE_SUPER_COMBAT;
		}

		if (divineStrPots.contains(itemId))
		{
			return PotionGroup.DIVINE_STRENGTH;
		}

		if (divineAtkPots.contains(itemId))
		{
			return PotionGroup.DIVINE_ATTACK;
		}

		if (regularRangingPots.contains(itemId))
		{
			return PotionGroup.RANGING;
		}

		if (regularSuperCombatPots.contains(itemId))
		{
			return PotionGroup.SUPER_COMBAT;
		}

		if (venomPots.contains(itemId))
		{
			return PotionGroup.ANTIVENOM;
		}

		if (poisonPots.contains(itemId))
		{
			return PotionGroup.ANTIPOISON;
		}

		return PotionGroup.UNKNOWN;
	}

	private int getPotionDose(int itemId)
	{
		String itemName = client.getItemDefinition(itemId).getName();
		if (itemName.contains("(1)"))
		{
			return 1;
		}

		if (itemName.contains("(2)"))
		{
			return 2;
		}

		if (itemName.contains("(3)"))
		{
			return 3;
		}

		return 4;
	}

	enum PotionGroup
	{
		DIVINE_BASTION,
		DIVINE_RANGING,
		DIVINE_SUPER_COMBAT,
		DIVINE_STRENGTH,
		DIVINE_ATTACK,
		RANGING,
		SUPER_COMBAT,
		PRAYER_REGEN,
		GOADING,
		ANTIPOISON,
		ANTIVENOM,
		COX_OVERLOAD,
		COX_ENHANCE,
		TOA_SALT,
		TOA_LIQUID_ADRENALINE,
		UNKNOWN
	}

	static class PotionOverlayState
	{
		final PotionGroup group;
		final int ticksUntilAllowed;
		final int dose;
		final boolean showTimer;

		PotionOverlayState(PotionGroup group, int ticksUntilAllowed, int dose)
		{
			this(group, ticksUntilAllowed, dose, true);
		}

		PotionOverlayState(PotionGroup group, int ticksUntilAllowed, int dose, boolean showTimer)
		{
			this.group = group;
			this.ticksUntilAllowed = ticksUntilAllowed;
			this.dose = dose;
			this.showTimer = showTimer;
		}
	}
}
