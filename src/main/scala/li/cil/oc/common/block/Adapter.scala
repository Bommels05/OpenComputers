package li.cil.oc.common.block

import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.blockentity
import li.cil.oc.integration.util.Wrench
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.{BlockGetter, Level, LevelReader}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class Adapter(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.ADAPTER) with traits.GUI {
  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.Adapter => ContainerTypes.openAdapterGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Adapter(blockentity.BlockEntityTypes.ADAPTER, pos, state)

  // ----------------------------------------------------------------------- //

  @Deprecated
  override def neighborChanged(state: BlockState, world: Level, pos: BlockPos, block: Block, fromPos: BlockPos, b: Boolean): Unit =
    world.getBlockEntity(pos) match {
      case adapter: blockentity.Adapter => adapter.neighborChanged()
      case _ => // Ignore.
    }

  override def onNeighborChange(state: BlockState, world: LevelReader, pos: BlockPos, neighbor: BlockPos) =
    world.getBlockEntity(pos) match {
      case adapter: blockentity.Adapter =>
        // TODO can we just pass the blockpos?
        val side =
          if (neighbor == (pos.below():BlockPos)) Direction.DOWN
          else if (neighbor == (pos.above():BlockPos)) Direction.UP
          else if (neighbor == (pos.north():BlockPos)) Direction.NORTH
          else if (neighbor == (pos.south():BlockPos)) Direction.SOUTH
          else if (neighbor == (pos.west():BlockPos)) Direction.WEST
          else if (neighbor == (pos.east():BlockPos)) Direction.EAST
          else throw new IllegalArgumentException("not a neighbor") // TODO wat
        adapter.neighborChanged(side)
      case _ => // Ignore.
    }

  override def localOnBlockActivated(world: Level, pos: BlockPos, player: Player, hand: InteractionHand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (Wrench.holdsApplicableWrench(player, pos)) {
      val sideToToggle = if (player.isCrouching) side.getOpposite else side
      world.getBlockEntity(pos) match {
        case adapter: blockentity.Adapter =>
          if (!world.isClientSide) {
            val oldValue = adapter.openSides(sideToToggle.ordinal())
            adapter.setSideOpen(sideToToggle, !oldValue)
          }
          true
        case _ => false
      }
    }
    else super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
  }
}
