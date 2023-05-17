package dev.xkmc.l2backpack.content.common;

import dev.xkmc.l2library.base.menu.SpriteManager;
import dev.xkmc.l2library.init.events.screen.base.ScreenTracker;
import dev.xkmc.l2library.init.events.screen.source.PlayerSlot;
import dev.xkmc.l2library.util.annotation.ServerOnly;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

public abstract class BaseBagContainer<T extends BaseBagContainer<T>> extends BaseOpenableContainer<T> {

	@ServerOnly
	private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<BaseBagContainer<?>>> MAP = new ConcurrentHashMap<>();

	protected final PlayerSlot<?> item_slot;
	protected final UUID uuid;

	public BaseBagContainer(MenuType<T> type, int windowId, Inventory inventory, SpriteManager manager,
							PlayerSlot<?> hand, UUID uuid, int row, Predicate<ItemStack> pred, @Nullable Component title) {
		super(type, windowId, inventory, manager, menu -> new BaseContainer<>(row * 9, menu), title);
		this.item_slot = hand;
		this.uuid = uuid;
		this.addSlot("grid", pred);
		if (!this.player.level.isClientSide()) {
			MAP.computeIfAbsent(uuid, e -> new ConcurrentLinkedQueue<>()).add(this);
			reload();
		}
	}

	@ServerOnly
	private void reload() {
		ItemStack stack = getStack();
		if (!stack.isEmpty()) {
			ListTag tag = BaseBagItem.getListTag(stack);
			if (tag.size() == 0) {
				CompoundTag ctag = stack.getOrCreateTag();
				if (ctag.contains("loot")) {
					ResourceLocation rl = new ResourceLocation(ctag.getString("loot"));
					ctag.remove("loot");
					if (player.level instanceof ServerLevel sl) {
						LootTable loottable = sl.getServer().getLootTables().get(rl);
						LootContext.Builder builder = new LootContext.Builder(sl);
						builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
						loottable.fill(container, builder.create(LootContextParamSets.EMPTY));
						save();
					}
				}
				return;
			}
			for (int i = 0; i < tag.size(); i++) {
				this.container.setItem(i, ItemStack.of((CompoundTag) tag.get(i)));
			}
		}
	}

	@Override
	public void slotsChanged(Container cont) {
		save();
	}

	@Override
	public void removed(Player player) {
		if (!player.level.isClientSide) {
			MAP.computeIfAbsent(uuid, e -> new ConcurrentLinkedQueue<>()).remove(this);
			save();
		}
		super.removed(player);
	}

	private void save() {
		if (player.getLevel().isClientSide()) return;
		ItemStack stack = getStack();
		if (!stack.isEmpty()) {
			serializeContents(stack);
		}
		MAP.computeIfAbsent(uuid, e -> new ConcurrentLinkedQueue<>()).forEach(BaseBagContainer::reload);
	}

	protected void serializeContents(ItemStack stack) {
		ListTag list = new ListTag();
		for (int i = 0; i < this.container.getContainerSize(); i++) {
			list.add(i, this.container.getItem(i).save(new CompoundTag()));
		}
		BaseBagItem.setListTag(stack, list);
	}

	private ItemStack stack_cache = ItemStack.EMPTY;

	@ServerOnly
	@Override
	public boolean stillValid(Player player) {
		ItemStack oldStack = stack_cache;
		ItemStack newStack = getStackRaw();
		return !getStackRaw().isEmpty() && oldStack == newStack;
	}

	public ItemStack getStack() {
		ItemStack stack = getStackRaw();
		if (stack.isEmpty()) return stack_cache;
		return stack;
	}

	private ItemStack getStackRaw() {
		ItemStack stack = item_slot.getItem(player);
		CompoundTag tag = stack.getTag();
		if (tag == null) return ItemStack.EMPTY;
		if (!tag.contains("container_id")) return ItemStack.EMPTY;
		if (!tag.getUUID("container_id").equals(uuid)) return ItemStack.EMPTY;
		stack_cache = stack;
		return stack;
	}

}
