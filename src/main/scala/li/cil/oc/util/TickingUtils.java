package li.cil.oc.util;

import li.cil.oc.common.blockentity.traits.Tickable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TickingUtils {

    //Is broken in scala
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> neededBlockEntityType) {
        return BaseEntityBlock.createTickerHelper(blockEntityType, neededBlockEntityType, (level, pos, state, entity) -> {
            if (entity instanceof Tickable) {
                ((Tickable) entity).tick();
            }
        });
    }

}
