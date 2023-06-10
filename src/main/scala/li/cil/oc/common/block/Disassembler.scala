package li.cil.oc.common.block

import java.util
import li.cil.oc.Settings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.blockentity
import li.cil.oc.util.Tooltip
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

import scala.collection.convert.ImplicitConversionsToScala._

class Disassembler(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.DISASSEMBLER) with traits.PowerAcceptor with traits.StateAware with traits.GUI {
  override protected def tooltipBody(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: TooltipFlag) {
    for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase, (Settings.get.disassemblerBreakChance * 100).toInt.toString)) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  // ----------------------------------------------------------------------- //

  override def energyThroughput = Settings.get.disassemblerRate

  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.Disassembler => ContainerTypes.openDisassemblerGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Disassembler(blockentity.BlockEntityTypes.DISASSEMBLER, pos, state)
}
