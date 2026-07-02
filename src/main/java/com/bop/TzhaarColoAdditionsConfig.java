package com.bop;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("TzhaarColoAdditions")
public interface TzhaarColoAdditionsConfig extends Config
{
	@ConfigItem(
		keyName = "hideInfernoPillars",
		name = "Hide Inferno pillars",
		description = "Removes the visible pillar objects in the Inferno",
		position = 0
	)
	default boolean hideInfernoPillars()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideColosseumPillars",
		name = "Hide Colosseum pillars",
		description = "Removes the visible pillar objects in the Fortis Colosseum",
		position = 1
	)
	default boolean hideColosseumPillars()
	{
		return true;
	}

	@ConfigItem(
		keyName = "markPillarTiles",
		name = "Mark pillar tiles",
		description = "Highlights the tiles underneath hidden pillars",
		position = 2
	)
	default boolean markPillarTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pillarMarkerStyle",
		name = "Pillar marker style",
		description = "Choose how hidden pillar tiles are marked",
		position = 3
	)
	default PillarMarkerStyle pillarMarkerStyle()
	{
		return PillarMarkerStyle.BORDERED_TILE;
	}

	@Alpha
	@ConfigItem(
		keyName = "pillarTileColor",
		name = "Pillar tile colour",
		description = "The colour used to highlight tiles underneath hidden pillars",
		position = 4
	)
	default Color pillarTileColor()
	{
		return new Color(
			40,
			40,
			40,
			150);
	}

	@ConfigItem(
		keyName = "hideInfernoOuterScene2",
		name = "Hide Inferno scenery",
		description = "Hides scenery and graphics objects outside the Inferno Arena. CAN BE BUGGY!",
		position = 5
	)
	default boolean hideInfernoOuterScene2()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideColosseumOuterScene2",
		name = "[experimental]Hide Colo scenery",
		description = "Hides (almost) everything outside the colosseum arena.",
		position = 6
	)
	default boolean hideColosseumOuterScene2()
	{
		return false;
	}
}
