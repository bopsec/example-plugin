package com.bop;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class NoMisclickRepotItemOverlay extends WidgetItemOverlay
{
	private static final Color TIMER_COLOR = Color.WHITE;
	private static final Color TIMER_SHADOW = Color.BLACK;

	private final NoMisclickRepotPlugin plugin;
	private final NoMisclickRepotConfig config;

	@Inject
	NoMisclickRepotItemOverlay(NoMisclickRepotPlugin plugin, NoMisclickRepotConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem item)
	{
		if (!config.blockedOverlay() && !config.repotTimer())
		{
			return;
		}

		NoMisclickRepotPlugin.PotionOverlayState state = plugin.getPotionOverlayState(itemId);
		if (state == null)
		{
			return;
		}

		Rectangle bounds = item.getCanvasBounds();
		if (config.blockedOverlay())
		{
			Color overlayColor = config.blockedOverlayColor();
			graphics.setColor(overlayColor);
			graphics.fill(bounds);
			graphics.setColor(new Color(overlayColor.getRed(), overlayColor.getGreen(), overlayColor.getBlue(), Math.min(180, overlayColor.getAlpha() + 80)));
			graphics.draw(bounds);
		}

		if (config.repotTimer() && state.showTimer && plugin.shouldShowTimer(item.getWidget().getIndex(), state))
		{
			renderTimer(graphics, bounds, state.ticksUntilAllowed);
		}
	}

	private void renderTimer(Graphics2D graphics, Rectangle bounds, int ticks)
	{
		Font previousFont = graphics.getFont();
		graphics.setFont(previousFont.deriveFont((float) Math.max(8, config.timerTextSize())));

		String text = Integer.toString(ticks);
		FontMetrics metrics = graphics.getFontMetrics();
		int x = bounds.x + bounds.width - metrics.stringWidth(text) - 2;
		int y = bounds.y + bounds.height - 3;

		graphics.setColor(TIMER_SHADOW);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(TIMER_COLOR);
		graphics.drawString(text, x, y);
		graphics.setFont(previousFont);
	}
}
