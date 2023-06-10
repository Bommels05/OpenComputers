package li.cil.oc.common.block

import li.cil.oc.Settings
import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class PowerConverter(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.POWER_CONVERTER) with traits.PowerAcceptor {
  override def energyThroughput: Double = Settings.get.powerConverterRate

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.PowerConverter(blockentity.BlockEntityTypes.POWER_CONVERTER, pos, state)
}
