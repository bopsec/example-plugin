package com.bop;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.vars.InputType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
        name = "Hotkey toggle sidepanel",
        description = "",
        tags = {"sidepanel", "fkey", "hotkey", "toggle"}
)
public class HotkeyHideSidepanelsPlugin extends Plugin {
	private static final String KEY_REMAPPING_PLUGIN_CLASS = "net.runelite.client.plugins.keyremapping.KeyRemappingPlugin";
	private static final String PRESS_ENTER_TO_CHAT = "Press Enter to Chat";

    @Inject
    private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

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
        public void keyPressed(KeyEvent e) {
			if (isInputActive())
			{
				return;
			}

			super.keyPressed(e);
        }

		@Override
		public void keyTyped(KeyEvent e)
		{
			if (isInputActive())
			{
				return;
			}

			super.keyTyped(e);
		}

        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

	private boolean isInputActive()
	{
		return client.getFocusedInputFieldWidget() != null
			|| isMessageLayerInputActive()
			|| chatboxPanelManager.getCurrentInput() != null
			|| isKeyRemappingTyping();
	}

	private boolean isMessageLayerInputActive()
	{
		return client.getVarcIntValue(VarClientID.MESLAYERMODE) != InputType.NONE.getType();
	}

	private boolean isKeyRemappingTyping()
	{
		if (!config.respectPressEnterToChat() || !isKeyRemappingActive())
		{
			return false;
		}

		if (!isKeyRemappingChatboxFocused())
		{
			return false;
		}

		Widget chatboxInput = client.getWidget(InterfaceID.Chatbox.INPUT);
		String chatboxText = chatboxInput == null ? null : chatboxInput.getText();
		return chatboxText != null && !chatboxText.contains(PRESS_ENTER_TO_CHAT);
	}

	private boolean isKeyRemappingChatboxFocused()
	{
		Widget chatboxParent = client.getWidget(InterfaceID.Chatbox.UNIVERSE);
		if (chatboxParent == null || chatboxParent.getOnKeyListener() == null)
		{
			return false;
		}

		Widget worldMapSearch = client.getWidget(InterfaceID.Worldmap.MAPLIST_DISPLAY);
		if (worldMapSearch != null && client.getVarcIntValue(VarClientID.WORLDMAP_SEARCHING) == 1)
		{
			return false;
		}

		Widget report = client.getWidget(InterfaceID.Reportabuse.UNIVERSE);
		if (report != null)
		{
			return false;
		}

		return client.getFocusedInputFieldWidget() == null;
	}

	private boolean isKeyRemappingActive()
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (KEY_REMAPPING_PLUGIN_CLASS.equals(plugin.getClass().getName()))
			{
				return pluginManager.isPluginActive(plugin);
			}
		}

		return false;
	}

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
