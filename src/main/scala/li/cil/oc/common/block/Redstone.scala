package li.cil.oc.common.block

import java.util
import li.cil.oc.common.blockentity
import li.cil.oc.integration.Mods
import li.cil.oc.util.Tooltip
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

import scala.collection.convert.ImplicitConversionsToScala._

class Redstone(props: Properties) extends RedstoneAware(props, blockentity.BlockEntityTypes.REDSTONE_IO) {
  override protected def tooltipTail(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: TooltipFlag) {
    super.tooltipTail(stack, world, tooltip, advanced)
    // todo more generic way for redstone mods to provide lines
  }

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Redstone(blockentity.BlockEntityTypes.REDSTONE_IO, pos, state)
}
