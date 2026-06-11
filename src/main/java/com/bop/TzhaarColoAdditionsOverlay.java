package com.bop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class TzhaarColoAdditionsOverlay extends Overlay
{
	private static final int TILE_FILL_ALPHA = 40;
	private static final float LINE_STROKE_WIDTH = 2f;

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

		Color markerColor = config.pillarTileColor();
		PillarMarkerStyle markerStyle = config.pillarMarkerStyle();

		for (TzhaarColoAdditionsPlugin.PillarArea pillarArea : plugin.getPillarAreas())
		{
			if (pillarArea.getPlane() != client.getPlane())
			{
				continue;
			}

			Polygon areaPoly = Perspective.getCanvasTileAreaPoly(
				client,
				pillarArea.getCenterLocalPoint(client.getTopLevelWorldView().getScene()),
				pillarArea.getSizeX(),
				pillarArea.getSizeY(),
				pillarArea.getPlane(),
				0
			);
			if (areaPoly != null)
			{
				renderMarker(graphics, areaPoly, markerColor, markerStyle);
			}
		}

		return null;
	}

	private void renderMarker(Graphics2D graphics, Polygon tilePoly, Color color, PillarMarkerStyle markerStyle)
	{
		switch (markerStyle)
		{
			case TILE_FILL:
				graphics.setColor(withAlpha(color, Math.max(color.getAlpha(), TILE_FILL_ALPHA)));
				graphics.fillPolygon(tilePoly);
				break;
			case LINE:
				Stroke originalStroke = graphics.getStroke();
				graphics.setColor(color);
				graphics.setStroke(new java.awt.BasicStroke(LINE_STROKE_WIDTH));
				graphics.drawPolygon(tilePoly);
				graphics.setStroke(originalStroke);
				break;
			case BORDERED_TILE:
			default:
				OverlayUtil.renderPolygon(graphics, tilePoly, color);
				break;
		}
	}

	private static Color withAlpha(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(alpha, 255));
	}
}
