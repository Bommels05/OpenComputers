package li.cil.oc.common.item.traits

import java.util
import li.cil.oc.Localization
import li.cil.oc.util.Tooltip
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

trait ItemTier extends SimpleItem {
  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, world: Level, tooltip: util.List[Component], flag: TooltipFlag) {
    super.appendHoverText(stack, world, tooltip, flag)
    if (flag.isAdvanced) {
      tooltip.add(new TextComponent(Localization.Tooltip.Tier(tierFromDriver(stack) + 1)).setStyle(Tooltip.DefaultStyle))
    }
  }
}
