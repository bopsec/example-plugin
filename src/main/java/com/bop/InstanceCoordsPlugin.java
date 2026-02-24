package com.bop;

import com.google.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Instance Coords",
        description = "Shows instance slot number and type",
        tags = {"instance", "coords"}
)
@Singleton
public class InstanceCoordsPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private InstanceCoordsOverlay overlay;

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
    }
}
