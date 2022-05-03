package dev.xkmc.l2backpack.init;

import dev.xkmc.l2backpack.content.backpack.BackpackItem;
import dev.xkmc.l2backpack.content.backpack.EnderBackpackItem;
import dev.xkmc.l2backpack.init.registrate.LightlandItems;
import dev.xkmc.l2library.repack.registrate.util.entry.ItemEntry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class LightLandClient {

	public static void onCtorClient(IEventBus bus, IEventBus eventBus) {
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		for (ItemEntry<BackpackItem> entry : LightlandItems.BACKPACKS) {
			ItemProperties.register(entry.get(), new ResourceLocation("open"), BackpackItem::isOpened);
		}
		ItemProperties.register(LightlandItems.ENDER_BACKPACK.get(), new ResourceLocation("open"), EnderBackpackItem::isOpened);
	}

}
