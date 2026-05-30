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

	@Alpha
	@ConfigItem(
		keyName = "pillarTileColor",
		name = "Pillar tile colour",
		description = "The colour used to highlight tiles underneath hidden pillars",
		position = 3
	)
	default Color pillarTileColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "hideInfernoOuterScene",
		name = "Hide Inferno outer scenery",
		description = "Hides scenery and graphics objects outside the inferred Inferno arena. The Ancestral Glyph NPC is not hidden.",
		position = 5
	)
	default boolean hideInfernoOuterScene()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideColosseumOuterScene",
		name = "Hide Colosseum outer scenery",
		description = "Hides selected scenery outside the Fortis Colosseum arena",
		position = 6
	)
	default boolean hideColosseumOuterScene()
	{
		return false;
	}
}
