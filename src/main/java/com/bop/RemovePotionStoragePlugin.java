package com.bop;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
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
	private static final int REMOVED_WIDGET_POSITION = -10_000;

	private static final int[] POTION_STORAGE_WIDGETS = {
		InterfaceID.Bankmain.STORAGE_POPUP_TAB,
		InterfaceID.Bankmain.STORAGE_POPUP_TAB_FRAME,
		InterfaceID.Bankmain.POTIONSTORE_BUTTON
	};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private final Boolean[] potionStorageWidgetsWereHidden = new Boolean[POTION_STORAGE_WIDGETS.length];
	private final int[] potionStorageWidgetsOriginalX = new int[POTION_STORAGE_WIDGETS.length];
	private final int[] potionStorageWidgetsOriginalY = new int[POTION_STORAGE_WIDGETS.length];
	private final int[] potionStorageWidgetsOriginalWidth = new int[POTION_STORAGE_WIDGETS.length];
	private final int[] potionStorageWidgetsOriginalHeight = new int[POTION_STORAGE_WIDGETS.length];

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(this::removePotionStorageWidgets);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(this::restorePotionStorageWidgets);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.BANKMAIN)
		{
			removePotionStorageWidgets();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		switch (scriptPostFired.getScriptId())
		{
			case ScriptID.BANKMAIN_INIT:
			case ScriptID.BANKMAIN_BUILD:
			case ScriptID.BANKMAIN_FINISHBUILDING:
			case ScriptID.BANKMAIN_POPUP_TAB_DRAW:
				removePotionStorageWidgets();
				break;
			default:
				break;
		}
	}

	private void removePotionStorageWidgets()
	{
		for (int i = 0; i < POTION_STORAGE_WIDGETS.length; i++)
		{
			Widget widget = client.getWidget(POTION_STORAGE_WIDGETS[i]);
			if (widget == null)
			{
				continue;
			}

			if (potionStorageWidgetsWereHidden[i] == null)
			{
				potionStorageWidgetsWereHidden[i] = widget.isSelfHidden();
				potionStorageWidgetsOriginalX[i] = widget.getOriginalX();
				potionStorageWidgetsOriginalY[i] = widget.getOriginalY();
				potionStorageWidgetsOriginalWidth[i] = widget.getOriginalWidth();
				potionStorageWidgetsOriginalHeight[i] = widget.getOriginalHeight();
			}

			widget.setHidden(true);
			widget.setPos(REMOVED_WIDGET_POSITION, REMOVED_WIDGET_POSITION);
			widget.setForcedPosition(REMOVED_WIDGET_POSITION, REMOVED_WIDGET_POSITION);
			widget.setSize(0, 0);
			revalidateWidgetAndParent(widget);
		}

		Widget itemsContainer = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
		if (itemsContainer != null)
		{
			itemsContainer.revalidate();
		}
	}

	private void restorePotionStorageWidgets()
	{
		for (int i = 0; i < POTION_STORAGE_WIDGETS.length; i++)
		{
			Widget widget = client.getWidget(POTION_STORAGE_WIDGETS[i]);
			if (widget != null && potionStorageWidgetsWereHidden[i] != null)
			{
				widget.setHidden(potionStorageWidgetsWereHidden[i]);
				widget.setPos(potionStorageWidgetsOriginalX[i], potionStorageWidgetsOriginalY[i]);
				widget.setForcedPosition(potionStorageWidgetsOriginalX[i], potionStorageWidgetsOriginalY[i]);
				widget.setSize(potionStorageWidgetsOriginalWidth[i], potionStorageWidgetsOriginalHeight[i]);
				revalidateWidgetAndParent(widget);
			}

			potionStorageWidgetsWereHidden[i] = null;
		}
	}

	private void revalidateWidgetAndParent(Widget widget)
	{
		widget.revalidate();

		Widget parent = widget.getParent();
		if (parent != null)
		{
			parent.revalidate();
		}
	}
}
