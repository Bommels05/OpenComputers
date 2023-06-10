package li.cil.oc.common.block

import java.util
import li.cil.oc.Settings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.block.property.PropertyRotatable
import li.cil.oc.common.blockentity
import li.cil.oc.util.Tooltip
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.{BlockState, StateDefinition}
import net.minecraft.world.level.material.FluidState

import scala.collection.convert.ImplicitConversionsToScala._

class Case(props: Properties, val tier: Int) extends RedstoneAware(props, blockentity.BlockEntityTypes.CASE) with traits.PowerAcceptor with traits.StateAware with traits.GUI {
  protected override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]) =
    builder.add(PropertyRotatable.Facing, property.PropertyRunning.Running)

  // ----------------------------------------------------------------------- //

  override protected def tooltipBody(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: TooltipFlag) {
    for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase, slots)) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  private def slots = tier match {
    case 0 => "2/1/1"
    case 1 => "2/2/2"
    case 2 | 3 => "3/2/3"
    case _ => "0/0/0"
  }

  // ----------------------------------------------------------------------- //

  override def energyThroughput = Settings.get.caseRate(tier)

  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.Case if te.stillValid(player) => ContainerTypes.openCaseGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Case(blockentity.BlockEntityTypes.CASE, tier, pos, state)

  // ----------------------------------------------------------------------- //

  override def localOnBlockActivated(world: Level, pos: BlockPos, player: Player, hand: InteractionHand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float) = {
    if (player.isCrouching) {
      if (!world.isClientSide) world.getBlockEntity(pos) match {
        case computer: blockentity.Case if !computer.machine.isRunning && computer.stillValid(player) => computer.machine.start()
        case _ =>
      }
      true
    }
    else super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
  }

  override def onDestroyedByPlayer(state: BlockState, world: Level, pos: BlockPos, player: Player, willHarvest: Boolean, fluid: FluidState): Boolean =
    world.getBlockEntity(pos) match {
      case c: blockentity.Case =>
        if (c.isCreative && (!player.isCreative || !c.canInteract(player.getName.getString))) false
        else c.canInteract(player.getName.getString) && super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid)
      case _ => super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid)
    }
}
