package li.cil.oc.common.block

import java.util
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.block.property.PropertyRotatable
import li.cil.oc.common.blockentity
import li.cil.oc.integration.Mods
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

import scala.collection.convert.ImplicitConversionsToScala._

class DiskDrive(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.DISK_DRIVE) with traits.GUI {
  protected override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]) =
    builder.add(PropertyRotatable.Facing)

  // ----------------------------------------------------------------------- //

  override protected def tooltipTail(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], flag: TooltipFlag) {
    super.tooltipTail(stack, world, tooltip, flag)
    if (Mods.ComputerCraft.isModAvailable) {
      for (curr <- Tooltip.get(getClass.getSimpleName + ".CC")) tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  // ----------------------------------------------------------------------- //

  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.DiskDrive => ContainerTypes.openDiskDriveGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.DiskDrive(blockentity.BlockEntityTypes.DISK_DRIVE, pos, state)

  // ----------------------------------------------------------------------- //

  override def hasAnalogOutputSignal(state: BlockState): Boolean = true

  override def getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int =
    world.getBlockEntity(pos) match {
      case drive: blockentity.DiskDrive if !drive.getItem(0).isEmpty => 15
      case _ => 0
    }

  // ----------------------------------------------------------------------- //

  override def localOnBlockActivated(world: Level, pos: BlockPos, player: Player, hand: InteractionHand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    // Behavior: sneaking -> Insert[+Eject], not sneaking -> GUI.
    if (player.isCrouching) world.getBlockEntity(pos) match {
      case drive: blockentity.DiskDrive =>
        val isDiskInDrive = drive.getItem(0) != null
        val isHoldingDisk = drive.canPlaceItem(0, heldItem)
        if (isDiskInDrive) {
          if (!world.isClientSide) {
            drive.dropSlot(0, 1, Option(drive.facing))
          }
        }
        if (isHoldingDisk) {
          // Insert the disk.
          drive.setItem(0, heldItem.split(1))
        }
        isDiskInDrive || isHoldingDisk
      case _ => false
    }
    else super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
  }
}
