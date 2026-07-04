package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Perspective;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarbitID;
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
	private static final int COLOSSEUM_BANK_REGION = 7316;
	private static final int COLOSSEUM_ARENA_MIN_X = 1808;
	private static final int COLOSSEUM_ARENA_MAX_X = 1840;
	private static final int COLOSSEUM_ARENA_MIN_Y = 3090;
	private static final int COLOSSEUM_ARENA_MAX_Y = 3123;
	private static final int TILEOBJECT_TYPE_SHIFT = 16;
	private static final int TILEOBJECT_TYPE_MASK = 0x7;
	private static final int TILEOBJECT_TYPE_OBJECT = 2;

	private static final Set<Integer> INFERNO_PILLARS_TO_MARK = Set.of(ObjectID.INFERNO_SAFESPOT_100, ObjectID.INFERNO_SAFESPOT_75,	ObjectID.INFERNO_SAFESPOT_50, ObjectID.INFERNO_SAFESPOT_25,	ObjectID.INFERNO_SAFESPOT1, ObjectID.INFERNO_SAFESPOT2,	ObjectID.INFERNO_SAFESPOT3);

	private static final Set<Integer> INFERNO_OBJECTS_TO_HIDE = Set.of(ObjectID.INFERNO_SAFESPOT_100, ObjectID.INFERNO_SAFESPOT_75, ObjectID.INFERNO_SAFESPOT_50, ObjectID.INFERNO_SAFESPOT_25, ObjectID.INFERNO_SAFESPOT1, ObjectID.INFERNO_SAFESPOT2, ObjectID.INFERNO_SAFESPOT3);

	private static final Set<Integer> INFERNO_PILLAR_DEATH_OBJECTS = Set.of(ObjectID.INFERNO_COLLAPSING_WALL_SAFESPOT_STATE1, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE1, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE1, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE2, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE2, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE3, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE3);

	private static final Set<Integer> COLOSSEUM_PILLARS = Set.of(ObjectID.PILLAR_CIVITAS01_COLOSSEUM01, ObjectID.PILLAR_CIVITAS01_COLOSSEUM02);

	private static final Set<Integer> INFERNO_SCENE_OBJECTS = Set.of(ObjectID.INFERNO_ENTRANCE_NOOP, ObjectID.INFERNO_ENTRANCE_OP, ObjectID.INFERNO_EXIT, ObjectID.INFERNO_SAFESPOT_100, ObjectID.INFERNO_SAFESPOT_75, ObjectID.INFERNO_SAFESPOT_50, ObjectID.INFERNO_SAFESPOT_25, ObjectID.INFERNO_ENTRANCE, ObjectID.INFERNO_SAFESPOT1, ObjectID.INFERNO_SAFESPOT2, ObjectID.INFERNO_SAFESPOT3);

	private static final Set<Integer> INFERNO_OUTER_SCENERY = Set.of(ObjectID.INFERNO_FLOOR_SAND_STRAIGHT_01, ObjectID.INFERNO_FLOOR_SAND_CORNER_01, ObjectID.INFERNO_FLOOR_SMALL_PLANE_01, ObjectID.INFERNO_FLOOR_SMALL_PLANE_02, ObjectID.INFERNO_FLOOR_LARGE_PLANE_01, ObjectID.INFERNO_ROOF_SMALL_PLANE_01, ObjectID.INFERNO_FLOOR_ROCKS_LAVA, ObjectID.INFERNO_FLOOR_ROCKS_01, ObjectID.INFERNO_FLOOR_ROCKS_02, ObjectID.INFERNO_FLOOR_ROCKS_03, ObjectID.INFERNO_FLOOR_ROCKS_04, ObjectID.INFERNO_FLOOR_LARGE_CORNER_01, ObjectID.INFERNO_FLOOR_LARGE_CORNER_02, ObjectID.INFERNO_FLOOR_LARGE_CORNER_03, ObjectID.INFERNO_FLOOR_LARGE_CORNER_04, ObjectID.INFERNO_FLOOR_LARGE_EDGE_01, ObjectID.INFERNO_FLOOR_LARGE_EDGE_02, ObjectID.INFERNO_FLOOR_LARGE_EDGE_03, ObjectID.INFERNO_PILLAR_EDGE_01, ObjectID.INFERNO_PILLAR_EDGE_02, ObjectID.INFERNO_PILLAR_EDGE_03, ObjectID.INFERNO_PILLAR_EDGE_04, ObjectID.INFERNO_PILLAR_EDGE_05, ObjectID.INFERNO_PILLAR_EDGE_06, ObjectID.INFERNO_FLOOR_WALL_LARGE_EDGE_01, ObjectID.INFERNO_FLOOR_WALL_LARGE_EDGE_02, ObjectID.INFERNO_FLOOR_WALL_LARGE_EDGE_03, ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_01, ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_02, ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_03, ObjectID.INFERNO_FLOOR_WALL_LARGE_CORNER_04, ObjectID.INFERNO_WALL_EDGE_LARGE_01, ObjectID.INFERNO_WALL_EDGE_LARGE_02, ObjectID.INFERNO_WALL_EDGE_LARGE_03, ObjectID.INFERNO_WALL_EDGE_LARGE_04, ObjectID.INFERNO_WALL_EDGE_LARGE_05, ObjectID.INFERNO_WALL_EDGE_LARGE_06, ObjectID.INFERNO_WALL_CORNER_LARGE_01, ObjectID.INFERNO_WALL_CORNER_LARGE_02, ObjectID.INFERNO_PRISON_WALL_EDGE_01, ObjectID.INFERNO_PRISON_WALL_EDGE_02, ObjectID.INFERNO_PRISON_WALL_EDGE_03, ObjectID.INFERNO_PRISON_WALL_CORNER_01, ObjectID.INFERNO_PRISON_ROOF, ObjectID.INFERNO_COLLAPSING_ADJACENT_WALL_FLOOR_CORNER_RIGHT_STATE1, ObjectID.INFERNO_COLLAPSING_ADJACENT_WALL_FLOOR_CORNER_LEFT_STATE1, ObjectID.INFERNO_COLLAPSING_ADJACENT_CORNER_JOIN_RIGHT_STATE1, ObjectID.INFERNO_COLLAPSING_ADJACENT_CORNER_JOIN_LEFT_STATE1, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE1, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE1, ObjectID.INFERNO_COLLAPSING_WALL_SAFESPOT_STATE1, ObjectID.INFERNO_PRISON_WALL_FLOOR_CORNER_RIGHT_STATE2, ObjectID.INFERNO_PRISON_WALL_FLOOR_CORNER_LEFT_STATE2, ObjectID.INFERNO_PRISON_WALL_CORNER_JOIN_RIGHT_STATE2, ObjectID.INFERNO_PRISON_WALL_CORNER_JOIN_LEFT_STATE2, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE2, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE2, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_RIGHT_STATE3, ObjectID.INFERNO_COLLAPSING_WALL_SIDE_LEFT_STATE3, ObjectID.INFERNO_FLOOR_LOWERED_01, ObjectID.INFERNO_FLOOR_LOWERED_02A, ObjectID.INFERNO_FLOOR_LOWERED_02B, ObjectID.INFERNO_FLOOR_LOWERED_03A, ObjectID.INFERNO_FLOOR_LOWERED_03B, ObjectID.INFERNO_COLLAPSING_PRISON_ROOF, ObjectID.INFERNO_FLOOR_FIXED_01, ObjectID.INFERNO_FLOOR_FIXED_02A, ObjectID.INFERNO_FLOOR_FIXED_02B, ObjectID.INFERNO_FLOOR_FIXED_03A, ObjectID.INFERNO_FLOOR_FIXED_03B);

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

	private final Set<PillarArea> pillarAreas = new HashSet<>();
	private final Set<PillarArea> infernoPillarAreas = new HashSet<>();
	private boolean inInfernoScene;

	private final RenderCallback renderCallback = new RenderCallback()
	{
		@Override
		public boolean drawObject(Scene scene, TileObject tileObject)
		{
			if (tileObject == null)
			{
				return true;
			}

			if (!isSceneObjectTag(tileObject))
			{
				return true;
			}

			checkInfernoScene(tileObject);
			return !shouldHide(tileObject.getId());
		}

		@Override
		public boolean drawTile(Scene scene, Tile tile)
		{
			return !shouldHideTile(scene, tile);
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
		clientThread.invoke(this::scanLoadedScene);
	}

	@Override
	protected void shutDown()
	{
		renderCallbackManager.unregister(renderCallback);
		overlayManager.remove(overlay);
		pillarAreas.clear();
		infernoPillarAreas.clear();
		inInfernoScene = false;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (config.hideColosseumOuterScene2() && (isInColosseumRegion() || isInColosseumBankRegion()))
		{
			if ((varbitChanged.getVarbitId() == VarbitID.MINIMAP_STATE && varbitChanged.getValue() == 0)
				|| (varbitChanged.getVarbitId() == VarbitID.GRAVESTONE_DURATION && varbitChanged.getValue() == 1500))
				reloadScene();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING
			|| gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			pillarAreas.clear();
			infernoPillarAreas.clear();
			inInfernoScene = false;
		}

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
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
			if ("markPillarTiles".equals(configChanged.getKey())
				|| "pillarMarkerStyle".equals(configChanged.getKey())
				|| "pillarTileColor".equals(configChanged.getKey()))
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
		pillarAreas.clear();
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
	}

	private boolean shouldHide(int objectId)
	{
		return config.hideInfernoPillars() && INFERNO_OBJECTS_TO_HIDE.contains(objectId)
			|| config.hideColosseumPillars() && isInColosseumRegion() && COLOSSEUM_PILLARS.contains(objectId)
			|| config.hideInfernoOuterScene2() && inInfernoScene && shouldHideInfernoOuterObject(objectId)
			|| config.hideColosseumOuterScene2() && isInColosseumRegion() && !COLOSSEUM_PILLARS.contains(objectId);
	}

	private boolean isSceneObjectTag(TileObject tileObject)
	{
		int sceneTagType = (int) (tileObject.getHash() >>> TILEOBJECT_TYPE_SHIFT) & TILEOBJECT_TYPE_MASK;
		return sceneTagType == TILEOBJECT_TYPE_OBJECT;
	}

	private boolean shouldHideTile(Scene scene, Tile tile)
	{
		if (!config.hideColosseumOuterScene2() || !isInColosseumRegion() || scene == null || tile == null)
		{
			return false;
		}

		if (isColosseumPillarTemplateTile(scene, tile) || isPillarTile(tile))
		{
			return false;
		}

		return isOutsideColosseumArenaBounds(scene, tile) || isMovementFullyBlocked(tile);
	}

	private boolean isColosseumPillarTemplateTile(Scene scene, Tile tile)
	{
		WorldPoint worldPoint = getInstanceTemplateWorldPoint(scene, tile);
		if (worldPoint == null)
		{
			return false;
		}

		int x = worldPoint.getX();
		int y = worldPoint.getY();
		return isInWorldArea(x, y, 1816, 3098, 1818, 3100)
			|| isInWorldArea(x, y, 1816, 3113, 1818, 3115)
			|| isInWorldArea(x, y, 1831, 3113, 1833, 3115)
			|| isInWorldArea(x, y, 1831, 3098, 1833, 3100);
	}

	private boolean isInWorldArea(int x, int y, int minX, int minY, int maxX, int maxY)
	{
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}

	private boolean isPillarTile(Tile tile)
	{
		int plane = tile.getPlane();
		int sceneX = tile.getSceneLocation().getX();
		int sceneY = tile.getSceneLocation().getY();

		for (PillarArea pillarArea : pillarAreas)
		{
			if (pillarArea.plane == plane
				&& sceneX >= pillarArea.minSceneX
				&& sceneX <= pillarArea.maxSceneX
				&& sceneY >= pillarArea.minSceneY
				&& sceneY <= pillarArea.maxSceneY)
			{
				return true;
			}
		}

		return false;
	}

	private boolean isOutsideColosseumArenaBounds(Scene scene, Tile tile)
	{
		WorldPoint worldPoint = getInstanceTemplateWorldPoint(scene, tile);
		if (worldPoint == null)
		{
			return false;
		}

		return worldPoint.getX() < COLOSSEUM_ARENA_MIN_X
			|| worldPoint.getX() > COLOSSEUM_ARENA_MAX_X
			|| worldPoint.getY() < COLOSSEUM_ARENA_MIN_Y
			|| worldPoint.getY() > COLOSSEUM_ARENA_MAX_Y
			|| isOutsideColosseumCornerBounds(worldPoint);
	}

	private boolean isOutsideColosseumCornerBounds(WorldPoint worldPoint)
	{
		int x = worldPoint.getX();
		int y = worldPoint.getY();

		return x == 1808 && (y < 3103 || y > 3110)
			|| x == 1809 && (y < 3099 || y > 3114)
			|| x == 1840 && (y < 3099 || y > 3114)
			|| y == 3090 && (x < 1821 || x > 1828)
			|| y == 3091 && (x < 1817 || x > 1832)
			|| y == 3122 && (x < 1817 || x > 1832)
			|| y == 3123 && (x < 1821 || x > 1828);
	}

	private WorldPoint getInstanceTemplateWorldPoint(Scene scene, Tile tile)
	{
		LocalPoint localPoint = tile.getLocalLocation();
		if (localPoint == null)
		{
			return null;
		}

		try
		{
			return WorldPoint.fromLocalInstance(scene, localPoint, tile.getPlane());
		}
		catch (RuntimeException ex)
		{
			return null;
		}
	}

	private boolean isMovementFullyBlocked(Tile tile)
	{
		CollisionData[] collisionMaps = client.getTopLevelWorldView().getCollisionMaps();
		int plane = tile.getPlane();
		if (plane < 0 || plane >= collisionMaps.length || collisionMaps[plane] == null)
		{
			return false;
		}

		int[][] flags = collisionMaps[plane].getFlags();
		int sceneX = tile.getSceneLocation().getX();
		int sceneY = tile.getSceneLocation().getY();
		if (sceneX < 0 || sceneY < 0 || sceneX >= flags.length || sceneY >= flags[sceneX].length)
		{
			return false;
		}

		return (flags[sceneX][sceneY] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0;
	}

	Set<PillarArea> getPillarAreas()
	{
		return Collections.unmodifiableSet(pillarAreas);
	}

	private void markPillarTiles(GameObject gameObject)
	{
		int plane = gameObject.getPlane();
		int minSceneX = gameObject.getSceneMinLocation().getX();
		int minSceneY = gameObject.getSceneMinLocation().getY();
		int maxSceneX = gameObject.getSceneMaxLocation().getX();
		int maxSceneY = gameObject.getSceneMaxLocation().getY();

		pillarAreas.add(new PillarArea(plane, minSceneX, minSceneY, maxSceneX, maxSceneY));

		if (INFERNO_PILLARS_TO_MARK.contains(gameObject.getId()))
		{
			infernoPillarAreas.add(new PillarArea(plane, minSceneX, minSceneY, maxSceneX, maxSceneY));
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
		pillarAreas.remove(pillarArea);
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
		if (!config.hideInfernoOuterScene2() || !inInfernoScene || graphicsObject == null)
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
		return isInInstance() && Arrays.stream(client.getTopLevelWorldView().getMapRegions()).anyMatch(region -> region == COLOSSEUM_REGION_ID);
	}

	private boolean isInColosseumBankRegion()
	{
		return !isInInstance() && Arrays.stream(client.getTopLevelWorldView().getMapRegions()).anyMatch(region -> region == COLOSSEUM_BANK_REGION);
	}

	private boolean isHideConfig(String key)
	{
		return "hideInfernoPillars".equals(key)
			|| "hideColosseumPillars".equals(key)
			|| "hideInfernoOuterScene2".equals(key)
			|| "hideColosseumOuterScene2".equals(key);
	}

	private boolean shouldReloadScene(String key)
	{
		return "hideInfernoPillars".equals(key)
			|| "hideColosseumPillars".equals(key)
			|| "hideInfernoOuterScene2".equals(key)
			|| "hideColosseumOuterScene2".equals(key);
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

	static final class PillarArea
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

		int getPlane()
		{
			return plane;
		}

		LocalPoint getCenterLocalPoint(Scene scene)
		{
			int sizeX = getSizeX();
			int sizeY = getSizeY();
			return LocalPoint.fromScene(minSceneX, minSceneY, scene)
				.plus(((sizeX - 1) * Perspective.LOCAL_TILE_SIZE) / 2, ((sizeY - 1) * Perspective.LOCAL_TILE_SIZE) / 2);
		}

		int getSizeX()
		{
			return maxSceneX - minSceneX + 1;
		}

		int getSizeY()
		{
			return maxSceneY - minSceneY + 1;
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
