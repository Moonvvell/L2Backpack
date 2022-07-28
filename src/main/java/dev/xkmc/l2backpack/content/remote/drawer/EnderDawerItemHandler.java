package dev.xkmc.l2backpack.content.remote.drawer;

import dev.xkmc.l2backpack.content.remote.DrawerAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public record EnderDawerItemHandler(DrawerAccess access) implements IItemHandler {

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		if (slot == 1) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(access.item(), access.getCount());
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.hasTag() || stack.getItem() != access.item()) return stack;
		int count = access.getCount();
		int max = access.item().getMaxStackSize() * EnderDrawerItem.MAX;
		int insert = Math.min(max - count, stack.getCount());
		if (!simulate) {
			access.setCount(count + insert);
		}
		return insert == stack.getCount() ? ItemStack.EMPTY : new ItemStack(access.item(), stack.getCount() - insert);
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		int count = access.getCount();
		int take = Math.min(count, amount);
		if (!simulate) {
			access.setCount(count - take);
		}
		return new ItemStack(access.item(), take);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64 * 64;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return !stack.hasTag() && stack.getItem() == access.item();
	}
}
