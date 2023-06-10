package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class CarpetedCapacitor(props: Properties) extends Capacitor(props, blockentity.BlockEntityTypes.CARPETED_CAPACITOR) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.CarpetedCapacitor(blockentity.BlockEntityTypes.CARPETED_CAPACITOR, pos, state)
}
