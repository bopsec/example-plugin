package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.VarClientID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
        name = "Hotkey toggle sidepanel",
        description = "",
        tags = {"sidepanel", "fkey", "hotkey", "toggle"}
)
public class HotkeyHideSidepanelsPlugin extends Plugin {
    @Inject
    private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

    @Inject
    private HotkeyHideSidepanelsConfig config;

	int savedTab = 3; // just defaulting to reopen in inventory just in case something caused it to not save

    @Provides
	HotkeyHideSidepanelsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HotkeyHideSidepanelsConfig.class);
    }

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(toggle);
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(toggle);
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.hotkey()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public void toggle() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
		int currentTab = client.getVarcIntValue(VarClientID.TOPLEVEL_PANEL);
		if (currentTab != -1) { // currentTab being -1 means it is currently closed
			savedTab = currentTab;
		}
		// we just run the same cs2 to open and close
		clientThread.invokeLater(() -> client.runScript(914, 1, 1131, savedTab)); // eta gameval.ScriptID?
    }


}
