package li.cil.oc.common.item.traits

import java.util
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.driver.item.MutableProcessor
import li.cil.oc.integration.opencomputers.DriverCPU
import li.cil.oc.util.Tooltip
import net.minecraft.Util
import net.minecraft.network.chat.{Component, TextComponent, TranslatableComponent}
import net.minecraft.world.{InteractionHand, InteractionResult, InteractionResultHolder}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{Item, ItemStack}
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.level.Level

import scala.collection.convert.ImplicitConversionsToScala._
import scala.language.existentials

trait CPULike extends SimpleItem {
  def cpuTier: Int

  override protected def tooltipData: Seq[Any] = Seq(Settings.get.cpuComponentSupport(cpuTier))

  override protected def tooltipExtended(stack: ItemStack, tooltip: util.List[Component]) {
    for (curr <- Tooltip.get("cpu.Architecture", api.Machine.getArchitectureName(DriverCPU.architecture(stack)))) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  override def use(stack: ItemStack, world: Level, player: Player): InteractionResultHolder[ItemStack] = {
    if (player.isCrouching) {
      if (!world.isClientSide) {
        api.Driver.driverFor(stack) match {
          case driver: MutableProcessor =>
            val architectures = driver.allArchitectures.toList
            if (architectures.nonEmpty) {
              val currentIndex = architectures.indexOf(driver.architecture(stack))
              val newIndex = (currentIndex + 1) % architectures.length
              val archClass = architectures(newIndex)
              val archName = api.Machine.getArchitectureName(archClass)
              driver.setArchitecture(stack, archClass)
              player.sendMessage(new TranslatableComponent(Settings.namespace + "tooltip.cpu.Architecture", archName), Util.NIL_UUID)
            }
            player.swing(InteractionHand.MAIN_HAND)
          case _ => // No known driver for this processor.
        }
      }
    }
    new InteractionResultHolder[ItemStack](InteractionResult.sidedSuccess(world.isClientSide), stack)
  }
}
