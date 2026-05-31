package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@PluginDescriptor(
	name = "Tzhaar/Colo Additions",
	description = "General QoL for Inferno and Colosseum",
	tags = {"inferno", "colosseum", "pillar", "hide", "tzhaar"}
)
public class TzhaarColoAdditionsPlugin extends Plugin
{
	private static final int COLOSSEUM_REGION_ID = 7216;

	private static final Set<Integer> INFERNO_PILLARS_TO_MARK = Set.of(
		ObjectID.INFERNO_SAFESPOT_100,
		ObjectID.INFERNO_SAFESPOT_75,
		ObjectID.INFERNO_SAFESPOT_50,
		ObjectID.INFERNO_SAFESPOT_25,
		ObjectID.INFERNO_SAFESPOT1,
		ObjectID.INFERNO_SAFESPOT2,
		ObjectID.INFERNO_SAFESPOT3
	);

	private static final Set<Integer> INFERNO_OBJECTS_TO_HIDE = Set.of(
		ObjectID.INFERNO_SAFESPOT_100,
		ObjectID.INFERNO_SAFESPOT_75,
		ObjectID.INFERNO_SAFESPOT_50,
		ObjectID.INFERNO_SAFESPOT_25,
		ObjectID.INFERNO_SAFESPOT1,
		ObjectID.INFERNO_SAFESPOT2,
		ObjectID.INFERNO_SAFESPOT3
	);

	private static final Set<Integer> INFERNO_PILLARS_TO_RENDER_HIDE_ONLY = Set.of(
		ObjectID.INFERNO_SAFESPOT_100,
		ObjectID.INFERNO_SAFESPOT_75,
		ObjectID.INFERNO_SAFESPOT_50,
		ObjectID.INFERNO_SAFESPOT_25,
		ObjectID.INFERNO_SAFESPOT1,
		ObjectID.INFERNO_SAFESPOT2,
		ObjectID.INFERNO_SAFESPOT3,
		ObjectID.INFERNO_COLLAPSING_WALL_SAFESPOT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE3,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE3
	);

	private static final Set<Integer> INFERNO_PILLAR_DEATH_OBJECTS = Set.of(
		ObjectID.INFERNO_COLLAPSING_WALL_SAFESPOT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE3,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE3
	);

	private static final Set<Integer> COLOSSEUM_PILLARS = Set.of(
		ObjectID.PILLAR_CIVITAS01_COLOSSEUM01,
		ObjectID.PILLAR_CIVITAS01_COLOSSEUM02
	);

	private static final Set<Integer> COLOSSEUM_OUTER_SCENERY = Set.of(
		ObjectID.WALLKIT_COLOSSEUM01_GATE02_CLOSED,
		ObjectID.WALLKIT_COLOSSEUM01_GATE02_CLOSED_M,
		ObjectID.WALLKIT_COLOSSEUM01_CORNER01,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL01,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL01_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL09,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL03,
		ObjectID.ICON_CIRCLE_WALL01,
		ObjectID.ICON_CIRCLE_WALL02,
		ObjectID.ICON_CIRCLE_WALL03,
		ObjectID.ICON_CIRCLE_WALL04,
		ObjectID.ICON_CIRCLE_WALL05,
		ObjectID.ICON_CIRCLE_WALL06,
		ObjectID.ICON_CIRCLE_WALL07,
		ObjectID.ICON_CIRCLE_WALL08,
		ObjectID.ICON_CIRCLE_WALL09,
		ObjectID.ICON_CIRCLE_WALL10,
		ObjectID.ICON_CIRCLE_WALL11,
		ObjectID.ICON_CIRCLE_WALL12,
		ObjectID.ICON_CIRCLE_WALL13,
		ObjectID.ICON_CIRCLE_WALL14,
		ObjectID.ICON_CIRCLE_WALL15,
		ObjectID.ICON_CIRCLE_WALL16,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR01A,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE08_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE07_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE08,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL04,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL05,
		ObjectID.ICON_DIAG_WALL01,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL06,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL07_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL03_M,
		ObjectID.WALLKIT_COLOSSEUM01_CORNER01_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE05_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE04_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE03_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE02_M,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR07A,
		ObjectID.WALLKIT_COLOSSEUM04_GATE02,
		ObjectID.WALLKIT_COLOSSEUM04_GATE05,
		ObjectID.WALLKIT_COLOSSEUM04_GATE06,
		ObjectID.WALLKIT_COLOSSEUM04_GATE04,
		ObjectID.WALLKIT_COLOSSEUM04_GATE06_M,
		ObjectID.WALLKIT_COLOSSEUM04_GATE05_M,
		ObjectID.COLOSSEUM_ENTRANCE_OUTSIDE,
		ObjectID.STATUES_COLOSSEUM01_GLADIATOR01,
		ObjectID.STATUES_COLOSSEUM01_GLADIATOR02,
		ObjectID.ROOFKIT_COLOSSEUM01_ENTRANCE01_6X2,
		ObjectID.WALLKIT_COLOSSEUM10_DEFAULT01,
		ObjectID.WALLKIT_COLOSSEUM10_DEFAULT02,
		ObjectID.WALLKIT_COLOSSEUM10_CORNER01,
		ObjectID.WALLKIT_COLOSSEUM10_ARCH01,
		ObjectID.WALLKIT_COLOSSEUM10_ARCH02,
		ObjectID.WALLKIT_COLOSSEUM10_ARCH01_M,
		ObjectID.WALLKIT_COLOSSEUM10_ARCH02_M,
		ObjectID.WALLKIT_COLOSSEUM10_WALLTOP01,
		ObjectID.WALLKIT_COLOSSEUM10_WALLTOP02,
		ObjectID.HS_INVISWALL,
		ObjectID.INVISABLE_NONBLOCKING_WALL_NOSHADOW,
		ObjectID.INVISWALL_ACTIVE_NOSHADOW,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02A,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02A_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02B,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02B_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02C,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL02C_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL04_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL05_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL07,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL08,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL08_M,
		ObjectID.WALLKIT_COLOSSEUM01_INTERNAL09_M,
		ObjectID.WALLKIT_COLOSSEUM01_GATE01,
		ObjectID.WALLKIT_COLOSSEUM01_GATE02_OPEN,
		ObjectID.WALLKIT_COLOSSEUM01_GATE02_OPEN_M,
		ObjectID.WALLKIT_COLOSSEUM01_GATE01_M,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR01,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR01_M,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR02,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR03,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR04,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR05,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR06,
		ObjectID.WALLKIT_COLOSSEUM01_EMPEROR06_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE01,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE01_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE02,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE02_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE02A,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE02A_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE03,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE03_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE04,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE04_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE05,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE05_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE06,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE07,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE07_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE08,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE08_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE09,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE09_M,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE10,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE12,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE13,
		ObjectID.WALLKIT_COLOSSEUM02_FENCE13_M,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR01,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR01_M,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR02,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR02_M,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR03,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR03_M,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR04,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR04_M,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR05,
		ObjectID.WALLKIT_COLOSSEUM02_EMPEROR05_M,
		ObjectID.WALLKIT_COLOSSEUM02_GATE01,
		ObjectID.WALLKIT_COLOSSEUM02_GATE01_M,
		ObjectID.WALLKIT_COLOSSEUM02_GATE02,
		ObjectID.WALLKIT_COLOSSEUM02_GATE03,
		ObjectID.WALLKIT_COLOSSEUM02_GATE03_M,
		ObjectID.WALLKIT_COLOSSEUM02_GATE04,
		ObjectID.WALLKIT_COLOSSEUM02_WALL01,
		ObjectID.WALLKIT_COLOSSEUM02_WALL01_M,
		ObjectID.WALLKIT_COLOSSEUM02_WALL02,
		ObjectID.WALLKIT_COLOSSEUM02_WALL02_M,
		ObjectID.WALLKIT_COLOSSEUM02_WALL03,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR01,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR02,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR03,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR03A,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR04,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR04A,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR05,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR05A,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR06,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR06A,
		ObjectID.ENTRANCE_COLOSSEUM01_FLOOR07,
		ObjectID.ENTRANCE_COLOSSEUM02_DOOR01,
		ObjectID.ENTRANCE_COLOSSEUM02_DOOR01_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE02,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE03,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE04,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE05,
		ObjectID.ENTRANCE_COLOSSEUM02_BACK02,
		ObjectID.ENTRANCE_COLOSSEUM02_DOOR02,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE06,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE06_M,
		ObjectID.ENTRANCE_COLOSSEUM02_SIDE07,
		ObjectID.ENTRANCE_COLOSSEUM02_CORNER01,
		ObjectID.ENTRANCE_COLOSSEUM02_CORNER01_M,
		ObjectID.ENTRANCE_COLOSSEUM03_BACK01,
		ObjectID.ENTRANCE_COLOSSEUM03_CORNER01,
		ObjectID.ENTRANCE_COLOSSEUM03_CORNER01_M,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE01,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE01_M,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE02,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE02_M,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE03,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE03_M,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE04,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE04_M,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE05,
		ObjectID.ENTRANCE_COLOSSEUM03_SIDE05_M,
		ObjectID.WALLKIT_COLOSSEUM03_WALL01,
		ObjectID.WALLKIT_COLOSSEUM03_WALL01_M,
		ObjectID.WALLKIT_COLOSSEUM03_ROOF01,
		ObjectID.WALLKIT_COLOSSEUM04_WALL01,
		ObjectID.WALLKIT_COLOSSEUM04_WALL01_M,
		ObjectID.WALLKIT_COLOSSEUM04_WALL02,
		ObjectID.WALLKIT_COLOSSEUM04_WALL02_M,
		ObjectID.WALLKIT_COLOSSEUM04_ENTRANCE01,
		ObjectID.WALLKIT_COLOSSEUM04_ENTRANCE01_M,
		ObjectID.WALLKIT_COLOSSEUM04_ENTRANCE02,
		ObjectID.WALLKIT_COLOSSEUM04_ENTRANCE02_M,
		ObjectID.WALLKIT_COLOSSEUM03_GATE01,
		ObjectID.WALLKIT_COLOSSEUM03_GATE01_M,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_M,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_CLOSED01,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_CLOSED01_M,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_OPEN01,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_OPEN01_M,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_OPEN02,
		ObjectID.WALLKIT_COLOSSEUM03_GATE02_OPEN02_M,
		ObjectID.WALLKIT_COLOSSEUM04_GATE01,
		ObjectID.WALLKIT_COLOSSEUM04_GATE01_M,
		ObjectID.WALLKIT_COLOSSEUM04_GATE03,
		ObjectID.WALLKIT_COLOSSEUM04_GATE03_M,
		ObjectID.CIVITAS_COLOSSEUM_BRAZIER01,
		ObjectID.CIVITAS_COLOSSEUM_BRAZIER02,
		ObjectID.PILLAR_CIVITAS01_COLOSSEUM01_SMALL01,
		ObjectID.ROOFKIT_COLOSSEUM01_FACADE01_6X1,
		ObjectID.ROOFKIT_COLOSSEUM01_FACADE02_6X1,
		ObjectID.ROOFKIT_COLOSSEUM_EMPEROR01,
		ObjectID.ROOFKIT_COLOSSEUM_EMPEROR02,
		ObjectID.ROOFKIT_COLOSSEUM_EMPEROR03,
		ObjectID.ROOFKIT_COLOSSEUM_EMPEROR04,
		ObjectID.CIVITAS_POTTED01_FAN01,
		ObjectID.CIVITAS_POTTED01_FAN02,
		ObjectID.CIVITAS_POTTED01_FAN03,
		ObjectID.CIVITAS_POTTED01_FAN04,
		ObjectID.THRONE_CIVITAS_COLOSSEUM01,
		ObjectID.THRONE_CIVITAS_COLOSSEUM01_BIG01,
		ObjectID.STATUES_CIVITAS01_ZYANYI01,
		ObjectID.STATUES_CIVITAS01_ZYANYI02,
		ObjectID.STATUES_CIVITAS01_ZYANYI03,
		ObjectID.STATUES_CIVITAS01_ZYANYI04,
		ObjectID.STATUES_CIVITAS01_ITZLA01,
		ObjectID.STATUES_CIVITAS01_ITZLA02,
		ObjectID.STATUES_CIVITAS01_ITZLA03,
		ObjectID.STATUES_CIVITAS01_FREJA01,
		ObjectID.STATUES_CIVITAS01_FREJA02,
		ObjectID.STATUES_CIVITAS01_FREJA03,
		ObjectID.STATUES_CIVITAS01_IMAFORE01,
		ObjectID.STATUES_CIVITAS01_IMAFORE02,
		ObjectID.STATUES_CIVITAS01_IMAFORE03,
		ObjectID.STATUES_CIVITAS01_CHARIOT01,
		ObjectID.STATUES_CIVITAS01_CHARIOT02,
		ObjectID.STATUES_CIVITAS01_CHARIOT03,
		ObjectID.STATUES_CIVITAS01_JAVELIN01,
		ObjectID.STATUES_CIVITAS01_JAVELIN02,
		ObjectID.STATUES_CIVITAS01_JAVELIN03,
		ObjectID.STATUES_CIVITAS01_MAXIMUS01,
		ObjectID.STATUES_CIVITAS01_MAXIMUS02,
		ObjectID.STATUES_CIVITAS01_MAXIMUS03,
		ObjectID.STATUES_CIVITAS01_SERPENT01,
		ObjectID.STATUES_CIVITAS01_SERPENT02,
		ObjectID.STATUES_CIVITAS01_JAGUAR01,
		ObjectID.STATUES_CIVITAS01_JAGUAR02
	);

	private static final Set<Integer> INFERNO_SCENE_OBJECTS = Set.of(
		ObjectID.INFERNO_ENTRANCE_NOOP,
		ObjectID.INFERNO_ENTRANCE_OP,
		ObjectID.INFERNO_EXIT,
		ObjectID.INFERNO_SAFESPOT_100,
		ObjectID.INFERNO_SAFESPOT_75,
		ObjectID.INFERNO_SAFESPOT_50,
		ObjectID.INFERNO_SAFESPOT_25,
		ObjectID.INFERNO_ENTRANCE,
		ObjectID.INFERNO_SAFESPOT1,
		ObjectID.INFERNO_SAFESPOT2,
		ObjectID.INFERNO_SAFESPOT3
	);

	private static final Set<Integer> INFERNO_OUTER_SCENERY = Set.of(
		ObjectID.INFERNO_FLOOR_SAND_STRAIGHT_01,
		ObjectID.INFERNO_FLOOR_SAND_CORNER_01,
		ObjectID.INFERNO_FLOOR_SMALL_PLANE_01,
		ObjectID.INFERNO_FLOOR_SMALL_PLANE_02,
		ObjectID.INFERNO_FLOOR_LARGE_PLANE_01,
		ObjectID.INFERNO_ROOF_SMALL_PLANE_01,
		ObjectID.INFERNO_FLOOR_ROCKS_LAVA,
		ObjectID.INFERNO_FLOOR_ROCKS_01,
		ObjectID.INFERNO_FLOOR_ROCKS_02,
		ObjectID.INFERNO_FLOOR_ROCKS_03,
		ObjectID.INFERNO_FLOOR_ROCKS_04,
		ObjectID.INFERNO_FLOOR_LARGE_CORNER_01,
		ObjectID.INFERNO_FLOOR_LARGE_CORNER_02,
		ObjectID.INFERNO_FLOOR_LARGE_CORNER_03,
		ObjectID.INFERNO_FLOOR_LARGE_CORNER_04,
		ObjectID.INFERNO_FLOOR_LARGE_EDGE_01,
		ObjectID.INFERNO_FLOOR_LARGE_EDGE_02,
		ObjectID.INFERNO_FLOOR_LARGE_EDGE_03,
		ObjectID.INFERNO_PILLAR_EDGE_01,
		ObjectID.INFERNO_PILLAR_EDGE_02,
		ObjectID.INFERNO_PILLAR_EDGE_03,
		ObjectID.INFERNO_PILLAR_EDGE_04,
		ObjectID.INFERNO_PILLAR_EDGE_05,
		ObjectID.INFERNO_PILLAR_EDGE_06,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_EDGE_01,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_EDGE_02,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_EDGE_03,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_01,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_02,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_03,
		ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_04,
		ObjectID.INFERNO_WALL_EDGE_LARGE_01,
		ObjectID.INFERNO_WALL_EDGE_LARGE_02,
		ObjectID.INFERNO_WALL_EDGE_LARGE_03,
		ObjectID.INFERNO_WALL_EDGE_LARGE_04,
		ObjectID.INFERNO_WALL_EDGE_LARGE_05,
		ObjectID.INFERNO_WALL_EDGE_LARGE_06,
		ObjectID.INFERNO_WALL_CORNER_LARGE_01,
		ObjectID.INFERNO_WALL_CORNER_LARGE_02,
		ObjectID.INFERNO_PRISON_WALL_EDGE_01,
		ObjectID.INFERNO_PRISON_WALL_EDGE_02,
		ObjectID.INFERNO_PRISON_WALL_EDGE_03,
		ObjectID.INFERNO_PRISON_WALL_CORNER_01,
		ObjectID.INFERNO_PRISON_ROOF,
		ObjectID.INFERNO_COLLAPSING_ADJACENT_WALL_FLOOR_CORNER_RIGHT_STATE1,
		ObjectID.INFERNO_COLLAPSING_ADJACENT_WALL_FLOOR_CORNER_LEFT_STATE1,
		ObjectID.INFERNO_COLLAPSING_ADJACENT_CORNER_JOIN_RIGHT_STATE1,
		ObjectID.INFERNO_COLLAPSING_ADJACENT_CORNER_JOIN_LEFT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE1,
		ObjectID.INFERNO_COLLAPSING_WALL_SAFESPOT_STATE1,
		ObjectID.INFERNO_PRISON_WALL_FLOOR_CORNER_RIGHT_STATE2,
		ObjectID.INFERNO_PRISON_WALL_FLOOR_CORNER_LEFT_STATE2,
		ObjectID.INFERNO_PRISON_WALL_CORNER_JOIN_RIGHT_STATE2,
		ObjectID.INFERNO_PRISON_WALL_CORNER_JOIN_LEFT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE2,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE3,
		ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE3,
		ObjectID.INFERNO_FLOOR_LOWERED_01,
		ObjectID.INFERNO_FLOOR_LOWERED_02A,
		ObjectID.INFERNO_FLOOR_LOWERED_02B,
		ObjectID.INFERNO_FLOOR_LOWERED_03A,
		ObjectID.INFERNO_FLOOR_LOWERED_03B,
		ObjectID.INFERNO_COLLAPSING_PRISON_ROOF,
		ObjectID.INFERNO_FLOOR_FIXED_01,
		ObjectID.INFERNO_FLOOR_FIXED_02A,
		ObjectID.INFERNO_FLOOR_FIXED_02B,
		ObjectID.INFERNO_FLOOR_FIXED_03A,
		ObjectID.INFERNO_FLOOR_FIXED_03B
	);

	@Inject
	private Client client;

	@Inject
	private TzhaarColoAdditionsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TzhaarColoAdditionsOverlay overlay;

	@Inject
	private RenderCallbackManager renderCallbackManager;

	@Inject
	private ClientThread clientThread;

	private final Set<PillarTile> pillarTiles = new HashSet<>();
	private final Set<PillarArea> infernoPillarAreas = new HashSet<>();
	private boolean inInfernoScene;
	private boolean removedGameObjects;
	private boolean refreshedColosseumScene;
	private boolean refreshingColosseumScene;

	private final RenderCallback renderCallback = new RenderCallback()
	{
		@Override
		public boolean drawObject(Scene scene, TileObject tileObject)
		{
			if (tileObject == null)
			{
				return true;
			}

			checkInfernoScene(tileObject);
			return !shouldHide(tileObject.getId());
		}

		@Override
		public boolean addEntity(Renderable renderable, boolean drawingUI)
		{
			if (renderable instanceof GraphicsObject)
			{
				GraphicsObject graphicsObject = (GraphicsObject) renderable;
				return !shouldHideGraphicsObject(graphicsObject);
			}

			return true;
		}
	};

	@Provides
	TzhaarColoAdditionsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TzhaarColoAdditionsConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		renderCallbackManager.register(renderCallback);
		clientThread.invoke(() ->
		{
			scanLoadedScene();
			reloadSceneIfNeeded();
		});
	}

	@Override
	protected void shutDown()
	{
		renderCallbackManager.unregister(renderCallback);
		overlayManager.remove(overlay);
		pillarTiles.clear();
		inInfernoScene = false;
		reloadSceneIfNeeded();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		int npcId = npcSpawned.getNpc().getId();
		if (npcId == NpcID.COLOSSEUM_BOSS_SEATED)
		{
			if ((config.hideColosseumPillars() || config.hideColosseumOuterScene()) && !refreshedColosseumScene)
			{
				refreshedColosseumScene = true;
				refreshingColosseumScene = true;
				reloadScene();
				return;
			}

			reloadSceneIfNeeded();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING
			|| gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			pillarTiles.clear();
			infernoPillarAreas.clear();
			inInfernoScene = false;
			removedGameObjects = false;

			if (!refreshingColosseumScene)
			{
				refreshedColosseumScene = false;
			}
		}

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			refreshingColosseumScene = false;
			scanLoadedScene();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		handleSpawnedTileObject(gameObjectSpawned.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		GameObject gameObject = gameObjectDespawned.getGameObject();
		if (gameObject != null && INFERNO_PILLARS_TO_MARK.contains(gameObject.getId()))
		{
			removeNearestPillarTileMarkers(gameObject);
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned)
	{
		handleSpawnedTileObject(wallObjectSpawned.getWallObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned)
	{
		handleSpawnedTileObject(decorativeObjectSpawned.getDecorativeObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		handleSpawnedTileObject(groundObjectSpawned.getGroundObject());
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectCreated)
	{
		shouldHideGraphicsObject(graphicsObjectCreated.getGraphicsObject());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if ("TzhaarColoAdditions".equals(configChanged.getGroup()))
		{
			if ("markPillarTiles".equals(configChanged.getKey()) || "pillarTileColor".equals(configChanged.getKey()))
			{
				return;
			}

			clientThread.invoke(() ->
			{
				scanLoadedScene();
				if (isHideConfig(configChanged.getKey()))
				{
					if (shouldReloadScene(configChanged.getKey()))
					{
						reloadScene();
					}
					else
					{
						reloadSceneIfNeeded();
					}
				}
			});
		}
	}

	private void scanLoadedScene()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Scene scene = client.getTopLevelWorldView().getScene();
		Tile[][][] tiles = scene.getTiles();
		inInfernoScene = false;
		pillarTiles.clear();
		infernoPillarAreas.clear();

		scanTiles(tiles);
		scanTiles(scene.getExtendedTiles());
	}

	private void scanTiles(Tile[][][] tiles)
	{
		for (Tile[][] value : tiles)
		{
			for (Tile[] item : value)
			{
				for (Tile tile : item)
				{
					if (tile == null)
					{
						continue;
					}

					for (GameObject gameObject : tile.getGameObjects())
					{
						handleSpawnedTileObject(gameObject);
					}
					handleSpawnedTileObject(tile.getWallObject());
					handleSpawnedTileObject(tile.getDecorativeObject());
					handleSpawnedTileObject(tile.getGroundObject());
				}
			}
		}
	}

	private void handleSpawnedTileObject(TileObject tileObject)
	{
		if (tileObject == null)
		{
			return;
		}

		if (isInfernoSceneObject(tileObject.getId()))
		{
			inInfernoScene = true;
		}

		if (tileObject instanceof GameObject
			&& (INFERNO_PILLARS_TO_MARK.contains(tileObject.getId()) || isInColosseumRegion() && COLOSSEUM_PILLARS.contains(tileObject.getId())))
		{
			markPillarTiles((GameObject) tileObject);
		}

		if (tileObject instanceof GameObject && INFERNO_PILLAR_DEATH_OBJECTS.contains(tileObject.getId()))
		{
			removeNearestPillarTileMarkers((GameObject) tileObject);
		}

		if (tileObject instanceof GameObject
			&& shouldHide(tileObject.getId())
			&& !INFERNO_PILLARS_TO_RENDER_HIDE_ONLY.contains(tileObject.getId())
			&& !COLOSSEUM_PILLARS.contains(tileObject.getId()))
		{
			GameObject gameObject = (GameObject) tileObject;
			client.getTopLevelWorldView().getScene().removeGameObject(gameObject);
			removedGameObjects = true;
		}
	}

	private boolean shouldHide(int objectId)
	{
		return config.hideInfernoPillars() && INFERNO_OBJECTS_TO_HIDE.contains(objectId)
			|| config.hideColosseumPillars() && isInColosseumRegion() && COLOSSEUM_PILLARS.contains(objectId)
			|| config.hideInfernoOuterScene() && inInfernoScene && shouldHideInfernoOuterObject(objectId)
			|| config.hideColosseumOuterScene() && isInColosseumRegion() && COLOSSEUM_OUTER_SCENERY.contains(objectId);
	}

	Set<PillarTile> getPillarTiles()
	{
		return Collections.unmodifiableSet(pillarTiles);
	}

	private void markPillarTiles(GameObject gameObject)
	{
		int plane = gameObject.getPlane();
		int minSceneX = gameObject.getSceneMinLocation().getX();
		int minSceneY = gameObject.getSceneMinLocation().getY();
		int maxSceneX = gameObject.getSceneMaxLocation().getX();
		int maxSceneY = gameObject.getSceneMaxLocation().getY();

		if (INFERNO_PILLARS_TO_MARK.contains(gameObject.getId()))
		{
			infernoPillarAreas.add(new PillarArea(plane, minSceneX, minSceneY, maxSceneX, maxSceneY));
		}

		for (int sceneX = minSceneX; sceneX <= maxSceneX; sceneX++)
		{
			for (int sceneY = minSceneY; sceneY <= maxSceneY; sceneY++)
			{
				pillarTiles.add(new PillarTile(LocalPoint.fromScene(sceneX, sceneY, client.getTopLevelWorldView().getScene()), plane));
			}
		}
	}

	private PillarArea findNearestInfernoPillarArea(GameObject gameObject)
	{
		int sceneX = (gameObject.getSceneMinLocation().getX() + gameObject.getSceneMaxLocation().getX()) / 2;
		int sceneY = (gameObject.getSceneMinLocation().getY() + gameObject.getSceneMaxLocation().getY()) / 2;
		int plane = gameObject.getPlane();

		PillarArea nearest = null;
		int nearestDistance = Integer.MAX_VALUE;
		for (PillarArea pillarArea : infernoPillarAreas)
		{
			if (pillarArea.plane != plane)
			{
				continue;
			}

			int distance = pillarArea.distanceTo(sceneX, sceneY);
			if (distance < nearestDistance)
			{
				nearest = pillarArea;
				nearestDistance = distance;
			}
		}

		return nearest;
	}

	private void removeNearestPillarTileMarkers(GameObject gameObject)
	{
		PillarArea pillarArea = findNearestInfernoPillarArea(gameObject);
		if (pillarArea == null)
		{
			return;
		}

		removePillarTileMarkers(pillarArea);
		infernoPillarAreas.remove(pillarArea);
	}

	private void removePillarTileMarkers(PillarArea pillarArea)
	{
		pillarTiles.removeIf(pillarArea::contains);
	}

	private boolean isInfernoSceneObject(int objectId)
	{
		return INFERNO_SCENE_OBJECTS.contains(objectId) || INFERNO_OUTER_SCENERY.contains(objectId);
	}

	private boolean shouldHideInfernoOuterObject(int objectId)
	{
		return INFERNO_OUTER_SCENERY.contains(objectId);
	}

	private boolean shouldHideGraphicsObject(GraphicsObject graphicsObject)
	{
		if (!config.hideInfernoOuterScene() || !inInfernoScene || graphicsObject == null)
		{
			return false;
		}

		return graphicsObject.getId() == SpotanimID.TZHAAR_INFERNO;
	}

	private void checkInfernoScene(TileObject tileObject)
	{
		if (tileObject != null && isInfernoSceneObject(tileObject.getId()))
		{
			inInfernoScene = true;
		}
	}

	private boolean isInInstance()
	{
		return client.getTopLevelWorldView().isInstance();
	}

	private boolean isInColosseumRegion()
	{
		return isInInstance() && Arrays.stream(client.getMapRegions()).anyMatch(region -> region == COLOSSEUM_REGION_ID);
	}

	private boolean isHideConfig(String key)
	{
		return "hideInfernoPillars".equals(key)
			|| "hideColosseumPillars".equals(key)
			|| "hideInfernoOuterScene".equals(key)
			|| "hideColosseumOuterScene".equals(key);
	}

	private boolean shouldReloadScene(String key)
	{
		return "hideInfernoPillars".equals(key)
			|| "hideColosseumPillars".equals(key);
	}

	private void reloadSceneIfNeeded()
	{
		if (!removedGameObjects)
		{
			return;
		}

		removedGameObjects = false;
		reloadScene();
	}

	private void reloadScene()
	{
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	static final class PillarTile
	{
		private final LocalPoint localPoint;
		private final int plane;

		private PillarTile(LocalPoint localPoint, int plane)
		{
			this.localPoint = localPoint;
			this.plane = plane;
		}

		LocalPoint getLocalPoint()
		{
			return localPoint;
		}

		int getPlane()
		{
			return plane;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (!(o instanceof PillarTile))
			{
				return false;
			}
			PillarTile that = (PillarTile) o;
			return plane == that.plane && Objects.equals(localPoint, that.localPoint);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(localPoint, plane);
		}
	}

	private static final class PillarArea
	{
		private final int plane;
		private final int minSceneX;
		private final int minSceneY;
		private final int maxSceneX;
		private final int maxSceneY;

		private PillarArea(int plane, int minSceneX, int minSceneY, int maxSceneX, int maxSceneY)
		{
			this.plane = plane;
			this.minSceneX = minSceneX;
			this.minSceneY = minSceneY;
			this.maxSceneX = maxSceneX;
			this.maxSceneY = maxSceneY;
		}

		private int distanceTo(int sceneX, int sceneY)
		{
			int centerX = (minSceneX + maxSceneX) / 2;
			int centerY = (minSceneY + maxSceneY) / 2;
			int dx = centerX - sceneX;
			int dy = centerY - sceneY;
			return dx * dx + dy * dy;
		}

		private boolean contains(PillarTile pillarTile)
		{
			return pillarTile.plane == plane
				&& pillarTile.localPoint.getSceneX() >= minSceneX
				&& pillarTile.localPoint.getSceneX() <= maxSceneX
				&& pillarTile.localPoint.getSceneY() >= minSceneY
				&& pillarTile.localPoint.getSceneY() <= maxSceneY;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (!(o instanceof PillarArea))
			{
				return false;
			}
			PillarArea that = (PillarArea) o;
			return plane == that.plane
				&& minSceneX == that.minSceneX
				&& minSceneY == that.minSceneY
				&& maxSceneX == that.maxSceneX
				&& maxSceneY == that.maxSceneY;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(plane, minSceneX, minSceneY, maxSceneX, maxSceneY);
		}
	}

}
