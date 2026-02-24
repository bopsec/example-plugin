package com.bop;

import com.google.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class InstanceCoordsOverlay extends OverlayPanel
{
    private final Client client;

    private enum InstanceType
    {
        SMALL,
        LARGE,
        BOAT,
        UNKNOWN
    }

    private static final int LARGE_STEP = 384;
    private static final int LARGE_FIRST_X = 6528;
    private static final int LARGE_LAST_X = 9984;
    private static final int LARGE_OFFSET_Y = 152;
    private static final int LARGE_COLS = (LARGE_LAST_X - LARGE_FIRST_X) / LARGE_STEP + 1;

    private static final int SMALL_STEP = 192;
    private static final int SMALL_FIRST_X = 10288;
    private static final int SMALL_LAST_X = 13936;
    private static final int SMALL_OFFSET_Y = 48;
    private static final int SMALL_COLS = (SMALL_LAST_X - SMALL_FIRST_X) / SMALL_STEP + 1;

    private static final int BOAT_STEP = 128;
    private static final int BOAT_FIRST_X = 14144;
    private static final int BOAT_LAST_X = 16192;
    private static final int BOAT_OFFSET_Y = 64;
    private static final int BOAT_COLS = (BOAT_LAST_X - BOAT_FIRST_X) / BOAT_STEP + 1;

    private static final int Y_WRAP = 16384;

    @Inject
    public InstanceCoordsOverlay(Client client)
    {
        this.client = client;

        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        if (client.getLocalPlayer() == null ||
                client.getLocalPlayer().getWorldView() == null)
        {
            return null;
        }

        panelComponent.getChildren().add(title("Instance Coords"));

        int baseX = client.getLocalPlayer().getWorldView().getBaseX();
        int baseY = client.getLocalPlayer().getWorldView().getBaseY();
        WorldPoint wp = client.getLocalPlayer().getWorldLocation();
        int trueX = wp.getX();
        int trueY = wp.getY();

        // Try true coords first, fall back to base coords
        InstanceType type = getInstanceType(trueX);
        int useX = trueX;
        int useY = trueY;

        if (type == InstanceType.UNKNOWN)
        {
            type = getInstanceType(baseX);
            useX = baseX;
            useY = baseY;
        }

        if (type == InstanceType.UNKNOWN)
        {
            panelComponent.getChildren().add(line("Status", "Not in instance"));
            return super.render(graphics);
        }

        SlotInfo info = getSlotInfo(useX, useY, type);

        panelComponent.getChildren().add(line("Type", type.name()));
        panelComponent.getChildren().add(line("True", trueX + ", " + trueY));
        panelComponent.getChildren().add(line("Base", baseX + ", " + baseY));

        if (info != null)
        {
            panelComponent.getChildren().add(line("Slot #", String.valueOf(info.index)));
            panelComponent.getChildren().add(line("Pos in slot", info.localX + ", " + info.localY));
        }
        else
        {
            panelComponent.getChildren().add(line("Slot #", "N/A"));
        }

        return super.render(graphics);
    }

    private TitleComponent title(String text)
    {
        return TitleComponent.builder().text(text).color(Color.CYAN).build();
    }

    private TitleComponent line(String label, String value)
    {
        return TitleComponent.builder().text(label + ": " + value).color(Color.WHITE).build();
    }

    private InstanceType getInstanceType(int x)
    {
        if (x >= BOAT_FIRST_X)
        {
            return InstanceType.BOAT;
        }

        if (x >= SMALL_FIRST_X)
        {
            return InstanceType.SMALL;
        }

        if (x >= LARGE_FIRST_X)
        {
            return InstanceType.LARGE;
        }

        return InstanceType.UNKNOWN;
    }

    private static class SlotInfo
    {
        int index;
        int localX;
        int localY;
    }

    private SlotInfo getSlotInfo(int x, int y, InstanceType type)
    {
        int step, firstX, offsetY, cols;

        switch (type)
        {
            case LARGE:
                step = LARGE_STEP;
                firstX = LARGE_FIRST_X;
                offsetY = LARGE_OFFSET_Y;
                cols = LARGE_COLS;
                break;

            case SMALL:
                step = SMALL_STEP;
                firstX = SMALL_FIRST_X;
                offsetY = SMALL_OFFSET_Y;
                cols = SMALL_COLS;
                break;

            case BOAT:
                step = BOAT_STEP;
                firstX = BOAT_FIRST_X;
                offsetY = BOAT_OFFSET_Y;
                cols = BOAT_COLS;
                break;

            default:
                return null;
        }

        if (x < firstX)
        {
            return null;
        }

        int column = (x - firstX) / step;
        int slotOriginX = firstX + (column * step);
        int localX = x - slotOriginX;

        int relY = y - offsetY;
        if (relY < 0)
        {
            relY += Y_WRAP;
        }
        int row = relY / step;
        int slotOriginY = offsetY + (row * step);
        if (slotOriginY >= Y_WRAP)
        {
            slotOriginY -= Y_WRAP;
        }
        int localY = y - slotOriginY;
        if (localY < 0)
        {
            localY += Y_WRAP;
        }

        SlotInfo info = new SlotInfo();
        info.index = (row * cols) + column;
        info.localX = localX;
        info.localY = localY;

        return info;
    }
}