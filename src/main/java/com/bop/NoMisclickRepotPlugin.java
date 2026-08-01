package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.VarbitChanged;
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
	private static final int ANTIPOISON_TIMER_ID = -1;
	private static final int ANTIVENOM_TIMER_ID = -2;
	private static final int TOA_LIQUID_ADRENALINE_TICKS = 250;

	@Inject
	private Client client;

	@Inject
	private NoMisclickRepotConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NoMisclickRepotItemOverlay itemOverlay;

	private final Map<Integer, Integer> remainingTicksByTimer = new HashMap<>();
	private final Set<Integer> timersUpdatedByVarbitThisTick = new HashSet<>();

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
	Antifire / Super antifire
	 */
	int antifire = VarbitID.ANTIFIRE_POTION;
	int superAntifire = VarbitID.SUPER_ANTIFIRE_POTION;
	List<Integer> antifirePots = List.of(
		ItemID._4DOSE1ANTIDRAGON, ItemID._3DOSE1ANTIDRAGON, ItemID._2DOSE1ANTIDRAGON, ItemID._1DOSE1ANTIDRAGON,
		ItemID.BRUTAL_2DOSE1ANTIDRAGON, ItemID.BRUTAL_1DOSE1ANTIDRAGON,
		ItemID._4DOSE2ANTIDRAGON, ItemID._3DOSE2ANTIDRAGON, ItemID._2DOSE2ANTIDRAGON, ItemID._1DOSE2ANTIDRAGON,
		ItemID.BRUTAL_2DOSE2ANTIDRAGON, ItemID.BRUTAL_1DOSE2ANTIDRAGON
	);
	List<Integer> superAntifirePots = List.of(
		ItemID._4DOSE3ANTIDRAGON, ItemID._3DOSE3ANTIDRAGON, ItemID._2DOSE3ANTIDRAGON, ItemID._1DOSE3ANTIDRAGON,
		ItemID.BRUTAL_2DOSE3ANTIDRAGON, ItemID.BRUTAL_1DOSE3ANTIDRAGON,
		ItemID._4DOSE4ANTIDRAGON, ItemID._3DOSE4ANTIDRAGON, ItemID._2DOSE4ANTIDRAGON, ItemID._1DOSE4ANTIDRAGON,
		ItemID.BRUTAL_2DOSE4ANTIDRAGON, ItemID.BRUTAL_1DOSE4ANTIDRAGON
	);

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
			ItemID.ANTIDOTE_1, ItemID.ANTIDOTE_2, ItemID.ANTIDOTE_3, ItemID.ANTIDOTE_4,
			ItemID.ANTIDOTE__1, ItemID.ANTIDOTE__2, ItemID.ANTIDOTE__3, ItemID.ANTIDOTE__4,
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

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == anti)
		{
			updatePoisonProtectionTimers(event.getValue());
			return;
		}

		int varbitId = event.getVarbitId();

		if (varbitId == divineBastion
			|| varbitId == divineRanging
			|| varbitId == divineScb
			|| varbitId == divineStr
			|| varbitId == divineAtk)
		{
			updateTimerFromVarbit(varbitId, client.getVarbitValue(varbitId));
			return;
		}

		if (varbitId == coxOverload)
		{
			updateTimerFromVarbit(coxOverload, client.getVarbitValue(coxOverload) * 25);
			return;
		}

		if (varbitId == antifire)
		{
			updateTimerFromVarbit(antifire, client.getVarbitValue(antifire) * 30);
			return;
		}

		if (varbitId == superAntifire)
		{
			updateTimerFromVarbit(superAntifire, client.getVarbitValue(superAntifire) * 20);
			return;
		}

		if (varbitId == prayerRegen)
		{
			updateTimerFromVarbit(prayerRegen, client.getVarbitValue(prayerRegen) * 12);
			return;
		}

		if (varbitId == goading)
		{
			updateTimerFromVarbit(goading, client.getVarbitValue(goading) * 6);
			return;
		}

		if (varbitId == coxEnhance || varbitId == coxPrayerEnhanceRate)
		{
			updateTimerFromVarbit(coxEnhance, getLiveCoxEnhanceRemainingTicks());
			return;
		}

		if (varbitId == toaOverload)
		{
			updateTimerFromVarbit(toaOverload, client.getVarbitValue(toaOverload) * 25);
			return;
		}

		if (varbitId == toaLiquidAdrenaline)
		{
			updateTimerFromVarbit(toaLiquidAdrenaline, getLiveToaLiquidAdrenalineRemainingTicks());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		for (Map.Entry<Integer, Integer> timer : remainingTicksByTimer.entrySet())
		{
			if (timersUpdatedByVarbitThisTick.contains(timer.getKey()))
			{
				continue;
			}

			timer.setValue(Math.max(0, timer.getValue() - 1));
		}

		timersUpdatedByVarbitThisTick.clear();
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
		remainingTicksByTimer.clear();
		timersUpdatedByVarbitThisTick.clear();
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

		int remainingTicks = getCachedRemainingTicks(goading, () -> client.getVarbitValue(goading) * 6);
		return goadingPots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isActiveAntifire(int itemId)
	{
		return config.antifire() && getAntifireRemainingTicks(itemId) > config.timeLeft();
	}

	private boolean isProtectedAntipoison(int itemId)
	{
		return config.antipoison() && isActivePoisonEffect(itemId);
	}

	private boolean isActiveTimedEffect(int itemId, List<Integer> itemIds, int varbitId)
	{
		return itemIds.contains(itemId) && getCachedRemainingTicks(varbitId) > config.timeLeft();
	}

	private boolean isActivePrayerRegenEffect(int itemId)
	{
		int remainingTicks = getCachedRemainingTicks(prayerRegen, () -> client.getVarbitValue(prayerRegen) * 12);
		return prayerRegenPots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isActivePoisonEffect(int itemId)
	{
		int poisonValue = client.getVarpValue(anti);
		if (poisonValue > 0)
		{
			return false;
		}

		int remainingTicks = getPoisonProtectionRemainingTicks(itemId);
		return remainingTicks > config.timeLeft();
	}

	private int getPoisonProtectionRemainingTicks(int itemId)
	{
		if (venomPots.contains(itemId))
		{
			return getCachedRemainingTicks(ANTIVENOM_TIMER_ID, this::getLiveAntivenomProtectionRemainingTicks);
		}

		if (poisonPots.contains(itemId))
		{
			return getCachedRemainingTicks(ANTIPOISON_TIMER_ID, this::getLiveAntipoisonProtectionRemainingTicks);
		}

		return 0;
	}

	private boolean isActiveCoxOverload(int itemId)
	{
		int remainingTicks = getCachedRemainingTicks(coxOverload, () -> client.getVarbitValue(coxOverload) * 25);
		return coxOverloadPots.contains(itemId) && remainingTicks > 0;
	}

	private boolean isActiveCoxPrayerEnhanceEffect(int itemId)
	{
		int remainingTicks = getCachedRemainingTicks(coxEnhance, this::getLiveCoxEnhanceRemainingTicks);
		return coxEnhancePots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isActiveToaSaltEffect(int itemId)
	{
		int remainingTicks = getCachedRemainingTicks(toaOverload, () -> client.getVarbitValue(toaOverload) * 25);
		return toaOverloadPots.contains(itemId) && remainingTicks > config.timeLeft();
	}

	private boolean isActiveToaLiquidAdrenalineEffect(int itemId)
	{
		int remainingTicks = getCachedRemainingTicks(toaLiquidAdrenaline, this::getLiveToaLiquidAdrenalineRemainingTicks);
		return toaLiquidAdrenalinePots.contains(itemId) && remainingTicks > config.timeLeft();
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
		return client.getBoostedSkillLevel(Skill.STRENGTH) > config.scbBoostThreshold()
			&& isSuperCombatAttackBoostAboveThreshold();
	}

	private boolean isRegularRangeBoostAboveThreshold()
	{
		return client.getBoostedSkillLevel(Skill.RANGED) > config.rangingPotionBoostThreshold();
	}

	private boolean isRegularSuperCombatBoostAboveThreshold()
	{
		return client.getBoostedSkillLevel(Skill.STRENGTH) > config.superCombatPotionBoostThreshold()
			&& isSuperCombatAttackBoostAboveThreshold();
	}

	private boolean isSuperCombatAttackBoostAboveThreshold()
	{
		return !config.superCombatAttackCheck()
			|| client.getBoostedSkillLevel(Skill.ATTACK) > config.superCombatAttackBoostThreshold();
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
			return createTimedOverlayState(getPotionGroup(itemId), getDivineRemainingTicks(itemId), getPotionDose(itemId));
		}

		if (isActiveNonTimedPotion(itemId))
		{
			return new PotionOverlayState(getPotionGroup(itemId), 0, getPotionDose(itemId), false);
		}

		if (isActivePrayerRegen(itemId))
		{
			return createTimedOverlayState(PotionGroup.PRAYER_REGEN, getCachedRemainingTicks(prayerRegen, () -> client.getVarbitValue(prayerRegen) * 12), getPotionDose(itemId));
		}

		if (isActiveGoading(itemId))
		{
			return createTimedOverlayState(PotionGroup.GOADING, getCachedRemainingTicks(goading, () -> client.getVarbitValue(goading) * 6), getPotionDose(itemId));
		}

		if (isActiveAntifire(itemId))
		{
			return createTimedOverlayState(getPotionGroup(itemId), getAntifireRemainingTicks(itemId), getPotionDose(itemId));
		}

		if (isProtectedAntipoison(itemId))
		{
			int remainingTicks = getPoisonProtectionRemainingTicks(itemId);
			return createTimedOverlayState(getPotionGroup(itemId), remainingTicks, getPotionDose(itemId));
		}

		if (config.coxOverload() && isActiveCoxOverload(itemId))
		{
			return new PotionOverlayState(PotionGroup.COX_OVERLOAD, getCachedRemainingTicks(coxOverload), getPotionDose(itemId));
		}

		if (config.coxEnhance() && isActiveCoxPrayerEnhanceEffect(itemId))
		{
			return createTimedOverlayState(PotionGroup.COX_ENHANCE, getCachedRemainingTicks(coxEnhance, this::getLiveCoxEnhanceRemainingTicks), getPotionDose(itemId));
		}

		if (config.toaSalt() && isActiveToaSaltEffect(itemId))
		{
			return createTimedOverlayState(PotionGroup.TOA_SALT, getCachedRemainingTicks(toaOverload, () -> client.getVarbitValue(toaOverload) * 25), getPotionDose(itemId));
		}

		if (config.toaLiquidAdren() && isActiveToaLiquidAdrenalineEffect(itemId))
		{
			return createTimedOverlayState(PotionGroup.TOA_LIQUID_ADRENALINE, getCachedRemainingTicks(toaLiquidAdrenaline, this::getLiveToaLiquidAdrenalineRemainingTicks), getPotionDose(itemId));
		}

		return null;
	}

	private PotionOverlayState createTimedOverlayState(PotionGroup group, int remainingEffectTicks, int dose)
	{
		return new PotionOverlayState(group, ticksUntilAllowed(remainingEffectTicks), remainingEffectTicks, dose);
	}

	PotionOverlayState getPotionTimerState(int itemId)
	{
		if (config.timerMode() == NoMisclickRepotConfig.TimerMode.REPOT_TIME)
		{
			return getPotionOverlayState(itemId);
		}

		return getPotionEffectTimerState(itemId);
	}

	private PotionOverlayState getPotionEffectTimerState(int itemId)
	{
		if (config.divines())
		{
			int remainingTicks = getDivineEffectRemainingTicks(itemId);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(getPotionGroup(itemId), remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.prayerRegen() && prayerRegenPots.contains(itemId))
		{
			int remainingTicks = getCachedRemainingTicks(prayerRegen, () -> client.getVarbitValue(prayerRegen) * 12);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(PotionGroup.PRAYER_REGEN, remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.goading() && goadingPots.contains(itemId))
		{
			int remainingTicks = getCachedRemainingTicks(goading, () -> client.getVarbitValue(goading) * 6);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(PotionGroup.GOADING, remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.antifire())
		{
			int remainingTicks = getAntifireRemainingTicks(itemId);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(getPotionGroup(itemId), remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.antipoison() && client.getVarpValue(anti) <= 0)
		{
			int remainingTicks = getPoisonProtectionRemainingTicks(itemId);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(getPotionGroup(itemId), remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.coxOverload() && coxOverloadPots.contains(itemId))
		{
			int remainingTicks = getCachedRemainingTicks(coxOverload, () -> client.getVarbitValue(coxOverload) * 25);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(PotionGroup.COX_OVERLOAD, remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.coxEnhance() && coxEnhancePots.contains(itemId))
		{
			int remainingTicks = getCachedRemainingTicks(coxEnhance, this::getLiveCoxEnhanceRemainingTicks);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(PotionGroup.COX_ENHANCE, remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.toaSalt() && toaOverloadPots.contains(itemId))
		{
			int remainingTicks = getCachedRemainingTicks(toaOverload, () -> client.getVarbitValue(toaOverload) * 25);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(PotionGroup.TOA_SALT, remainingTicks, getPotionDose(itemId));
			}
		}

		if (config.toaLiquidAdren() && toaLiquidAdrenalinePots.contains(itemId))
		{
			int remainingTicks = getCachedRemainingTicks(toaLiquidAdrenaline, this::getLiveToaLiquidAdrenalineRemainingTicks);
			if (remainingTicks > 0)
			{
				return createTimedOverlayState(PotionGroup.TOA_LIQUID_ADRENALINE, remainingTicks, getPotionDose(itemId));
			}
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
			PotionOverlayState itemState = getPotionTimerState(items[i].getId());
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

	private int getAntifireRemainingTicks(int itemId)
	{
		int superAntifireRemainingTicks = getCachedRemainingTicks(superAntifire, () -> client.getVarbitValue(superAntifire) * 20);
		if (superAntifirePots.contains(itemId))
		{
			return superAntifireRemainingTicks;
		}

		if (antifirePots.contains(itemId))
		{
			int antifireRemainingTicks = getCachedRemainingTicks(antifire, () -> client.getVarbitValue(antifire) * 30);
			return Math.max(antifireRemainingTicks, superAntifireRemainingTicks);
		}

		return 0;
	}

	private int getDivineRemainingTicks(int itemId)
	{
		int remainingTicks = 0;
		if (rangingPotionPots.contains(itemId) && isRangeBoostAboveThreshold())
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineBastion));
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineRanging));
		}

		if (divineScbPots.contains(itemId) && isScbBoostAboveThreshold())
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineScb));
		}

		if (divineStrPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineStr));
		}

		if (divineAtkPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineAtk));
		}

		return remainingTicks;
	}

	private int getDivineEffectRemainingTicks(int itemId)
	{
		int remainingTicks = 0;
		if (rangingPotionPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineBastion));
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineRanging));
		}

		if (divineScbPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineScb));
		}

		if (divineStrPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineStr));
		}

		if (divineAtkPots.contains(itemId))
		{
			remainingTicks = Math.max(remainingTicks, getCachedRemainingTicks(divineAtk));
		}

		return remainingTicks;
	}

	private void updateTimerFromVarbit(int timerId, int remainingTicks)
	{
		remainingTicksByTimer.put(timerId, remainingTicks);
		timersUpdatedByVarbitThisTick.add(timerId);
	}

	private void updatePoisonProtectionTimers(int poisonValue)
	{
		updateTimerFromVarbit(ANTIPOISON_TIMER_ID, getLiveAntipoisonProtectionRemainingTicks(poisonValue));
		updateTimerFromVarbit(ANTIVENOM_TIMER_ID, getLiveAntivenomProtectionRemainingTicks(poisonValue));
	}

	private int getCachedRemainingTicks(int varbitId)
	{
		return getCachedRemainingTicks(varbitId, () -> client.getVarbitValue(varbitId));
	}

	private int getCachedRemainingTicks(int timerId, TimerValueProvider liveValueProvider)
	{
		return remainingTicksByTimer.computeIfAbsent(timerId, ignored -> liveValueProvider.get());
	}

	private int getLiveCoxEnhanceRemainingTicks()
	{
		return client.getVarbitValue(coxEnhance) * client.getVarbitValue(coxPrayerEnhanceRate);
	}

	private int getLiveToaLiquidAdrenalineRemainingTicks()
	{
		return client.getVarbitValue(toaLiquidAdrenaline) > 0 ? TOA_LIQUID_ADRENALINE_TICKS : 0;
	}

	private int getLiveAntipoisonProtectionRemainingTicks()
	{
		return getLiveAntipoisonProtectionRemainingTicks(client.getVarpValue(anti));
	}

	private int getLiveAntipoisonProtectionRemainingTicks(int poisonValue)
	{
		return Math.max(0, -poisonValue * 30);
	}

	private int getLiveAntivenomProtectionRemainingTicks()
	{
		return getLiveAntivenomProtectionRemainingTicks(client.getVarpValue(anti));
	}

	private int getLiveAntivenomProtectionRemainingTicks(int poisonValue)
	{
		return Math.max(0, (-poisonValue - 38) * 30);
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

		if (superAntifirePots.contains(itemId))
		{
			return PotionGroup.SUPER_ANTIFIRE;
		}

		if (antifirePots.contains(itemId))
		{
			return PotionGroup.ANTIFIRE;
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
		ANTIFIRE,
		SUPER_ANTIFIRE,
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
		final int remainingEffectTicks;
		final int dose;
		final boolean showTimer;

		PotionOverlayState(PotionGroup group, int ticksUntilAllowed, int dose)
		{
			this(group, ticksUntilAllowed, ticksUntilAllowed, dose, true);
		}

		PotionOverlayState(PotionGroup group, int ticksUntilAllowed, int dose, boolean showTimer)
		{
			this(group, ticksUntilAllowed, ticksUntilAllowed, dose, showTimer);
		}

		PotionOverlayState(PotionGroup group, int ticksUntilAllowed, int remainingEffectTicks, int dose)
		{
			this(group, ticksUntilAllowed, remainingEffectTicks, dose, true);
		}

		PotionOverlayState(PotionGroup group, int ticksUntilAllowed, int remainingEffectTicks, int dose, boolean showTimer)
		{
			this.group = group;
			this.ticksUntilAllowed = ticksUntilAllowed;
			this.remainingEffectTicks = remainingEffectTicks;
			this.dose = dose;
			this.showTimer = showTimer;
		}
	}

	private interface TimerValueProvider
	{
		int get();
	}
}
