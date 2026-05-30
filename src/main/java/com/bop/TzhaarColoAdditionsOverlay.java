package com.bop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class TzhaarColoAdditionsOverlay extends Overlay
{
	private final Client client;
	private final TzhaarColoAdditionsConfig config;
	private final TzhaarColoAdditionsPlugin plugin;

	@Inject
	TzhaarColoAdditionsOverlay(Client client, TzhaarColoAdditionsConfig config, TzhaarColoAdditionsPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.markPillarTiles())
		{
			return null;
		}

		Color baseColor = config.pillarTileColor();
		Color tileColor = new Color(
			baseColor.getRed(),
			baseColor.getGreen(),
			baseColor.getBlue(),
			baseColor.getAlpha()
		);

		for (TzhaarColoAdditionsPlugin.PillarTile pillarTile : plugin.getPillarTiles())
		{
			if (pillarTile.getPlane() != client.getPlane())
			{
				continue;
			}

			Polygon tilePoly = Perspective.getCanvasTilePoly(client, pillarTile.getLocalPoint());
			if (tilePoly != null)
			{
				OverlayUtil.renderPolygon(graphics, tilePoly, tileColor);
			}
		}

		return null;
	}
}
