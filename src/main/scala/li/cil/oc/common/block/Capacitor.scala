package li.cil.oc.common.block

import java.util.Random
import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class Capacitor(props: Properties, blockEntityType: BlockEntityType[_]) extends SimpleBlock(props, blockEntityType) {

  def this(props: Properties) {
    this(props, blockentity.BlockEntityTypes.CAPACITOR)
  }

  @Deprecated
  override def isRandomlyTicking(state: BlockState) = true

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Capacitor(blockentity.BlockEntityTypes.CAPACITOR, pos, state)

  // ----------------------------------------------------------------------- //

  override def hasAnalogOutputSignal(state: BlockState): Boolean = true

  override def getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int =
    world.getBlockEntity(pos) match {
      case capacitor: blockentity.Capacitor if !world.isClientSide =>
        math.round(15 * capacitor.node.localBuffer / capacitor.node.localBufferSize).toInt
      case _ => 0
    }

  override def tick(state: BlockState, world: ServerLevel, pos: BlockPos, rand: Random): Unit = {
    world.updateNeighborsAt(pos, this)
  }

  @Deprecated
  override def neighborChanged(state: BlockState, world: Level, pos: BlockPos, block: Block, fromPos: BlockPos, b: Boolean): Unit =
    world.getBlockEntity(pos) match {
      case capacitor: blockentity.Capacitor => capacitor.recomputeCapacity()
      case _ =>
    }
}
