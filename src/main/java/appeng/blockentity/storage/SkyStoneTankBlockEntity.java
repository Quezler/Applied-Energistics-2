package appeng.blockentity.storage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.AEBaseBlockEntity;

public class SkyStoneTankBlockEntity extends AEBaseBlockEntity {

    public static final int BUCKET_CAPACITY = 16;

    private final SingleVariantStorage<FluidVariant> storage = new SingleVariantStorage<>() {

        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET * BUCKET_CAPACITY;
        }

        @Override
        protected void onFinalCommit() {
            SkyStoneTankBlockEntity.this.markForUpdate();
            SkyStoneTankBlockEntity.this.setChanged();
        }
    };

    public SkyStoneTankBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.put("variant", storage.variant.toNbt());
        data.putLong("amount", storage.amount);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        storage.variant = FluidVariant.fromNbt(data.getCompound("variant"));
        storage.amount = data.getLong("amount");
    }

    public boolean onPlayerUse(Player player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, InteractionHand.MAIN_HAND)
                .find(FluidStorage.ITEM);
        if (handIo != null) {
            // move from hand into this tank
            if (StorageUtil.move(handIo, storage, f -> true, Long.MAX_VALUE, null) > 0)
                return true;
            // move from this tank into hand
            if (StorageUtil.move(storage, handIo, f -> true, Long.MAX_VALUE, null) > 0)
                return true;
        }
        return false;
    }

    public Storage<FluidVariant> getStorage(Direction direction) {
        return storage;
    }

    public SingleVariantStorage<FluidVariant> getStorage() {
        return storage;
    }

    protected boolean readFromStream(FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        storage.amount = data.readLong();
        storage.variant = FluidVariant.fromNbt(data.readNbt());
        return ret;
    }

    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeLong(storage.amount);
        data.writeNbt(storage.getResource().toNbt());

    }
}
