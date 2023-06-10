package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class Geolyzer(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.GEOLYZER) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Geolyzer(blockentity.BlockEntityTypes.GEOLYZER, pos, state)
}
