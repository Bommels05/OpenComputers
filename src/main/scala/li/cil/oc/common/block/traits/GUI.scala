package li.cil.oc.common.block.traits

import li.cil.oc.common.block.SimpleBlock
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.{InteractionHand, MenuProvider}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

trait GUI extends SimpleBlock {
  def openGui(player: ServerPlayer, world: Level, pos: BlockPos)

  // This gets forwarded to the vanilla PlayerEntity.openMenu call which doesn't support extra data.
  override def getMenuProvider(state: BlockState, world: Level, pos: BlockPos): MenuProvider = null

  override def localOnBlockActivated(world: Level, pos: BlockPos, player: Player, hand: InteractionHand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!player.isCrouching) {
      player match {
        case srvPlr: ServerPlayer if !world.isClientSide => openGui(srvPlr, world, pos)
        case _ =>
      }
      true
    }
    else super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
  }
}
