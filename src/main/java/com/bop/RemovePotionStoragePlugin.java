package com.bop;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Remove potion storage",
	description = "Removes the potion storage icon in bank"
)
public class RemovePotionStoragePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private Boolean storagePopupTabWasHidden;

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(this::hideStoragePopupTab);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(this::restoreStoragePopupTab);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.BANKMAIN)
		{
			clientThread.invokeLater(this::hideStoragePopupTab);
		}
	}

	private void hideStoragePopupTab()
	{
		Widget storagePopupTab = client.getWidget(InterfaceID.Bankmain.STORAGE_POPUP_TAB);
		if (storagePopupTab == null)
		{
			return;
		}

		if (storagePopupTabWasHidden == null)
		{
			storagePopupTabWasHidden = storagePopupTab.isSelfHidden();
		}

		storagePopupTab.setHidden(true);
		storagePopupTab.revalidate();

		Widget parent = storagePopupTab.getParent();
		if (parent != null)
		{
			parent.revalidate();
		}
	}

	private void restoreStoragePopupTab()
	{
		Widget storagePopupTab = client.getWidget(InterfaceID.Bankmain.STORAGE_POPUP_TAB);
		if (storagePopupTab != null && storagePopupTabWasHidden != null)
		{
			storagePopupTab.setHidden(storagePopupTabWasHidden);
			storagePopupTab.revalidate();
		}

		storagePopupTabWasHidden = null;
	}
}
