package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

abstract class RedstoneAware(props: Properties, blockEntityType: BlockEntityType[_]) extends SimpleBlock(props, blockEntityType) {
  override def isSignalSource(state: BlockState): Boolean = true

  override def canConnectRedstone(state: BlockState, world: BlockGetter, pos: BlockPos, side: Direction): Boolean =
    world.getBlockEntity(pos) match {
      case redstone: blockentity.traits.RedstoneAware => redstone.isOutputEnabled
      case _ => false
    }

  override def getDirectSignal(state: BlockState, world: BlockGetter, pos: BlockPos, side: Direction) =
    getSignal(state, world, pos, side)

  @Deprecated
  override def getSignal(state: BlockState, world: BlockGetter, pos: BlockPos, side: Direction) =
    world.getBlockEntity(pos) match {
      case redstone: blockentity.traits.RedstoneAware if side != null => redstone.getOutput(side.getOpposite) max 0
      case _ => super.getSignal(state, world, pos, side)
    }

  // ----------------------------------------------------------------------- //

  @Deprecated
  override def neighborChanged(state: BlockState, world: Level, pos: BlockPos, block: Block, fromPos: BlockPos, b: Boolean): Unit = {
    world.getBlockEntity(pos) match {
      case redstone: blockentity.traits.RedstoneAware => redstone.checkRedstoneInputChanged()
      case _ => // Ignore.
    }
  }
}
