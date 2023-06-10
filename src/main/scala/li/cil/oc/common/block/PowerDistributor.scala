package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class PowerDistributor(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.POWER_DISTRIBUTOR) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.PowerDistributor(blockentity.BlockEntityTypes.POWER_DISTRIBUTOR, pos, state)
}

