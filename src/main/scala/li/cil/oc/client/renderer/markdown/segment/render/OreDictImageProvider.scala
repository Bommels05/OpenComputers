package li.cil.oc.client.renderer.markdown.segment.render

import li.cil.oc.api.manual.ImageProvider
import li.cil.oc.api.manual.ImageRenderer
import li.cil.oc.api.manual.InteractiveImageRenderer
import li.cil.oc.client.Textures
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags._
import net.minecraft.world.item.{Item, ItemStack}
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.tags.ITag

import scala.collection.mutable
import scala.collection.convert.ImplicitConversionsToScala._

object OreDictImageProvider extends ImageProvider {
  override def getImage(data: String): ImageRenderer = {
    val desired = new ResourceLocation(data.toLowerCase)
    val stacks = mutable.ArrayBuffer.empty[ItemStack]
    ForgeRegistries.ITEMS.tags().getTag(ForgeRegistries.ITEMS.tags().createTagKey(desired)) match {
      case tag: ITag[Item] => stacks ++= tag.stream().toList.map(new ItemStack(_))
      case _ =>
    }
    if (stacks.isEmpty) {
      ForgeRegistries.BLOCKS.tags().getTag(ForgeRegistries.BLOCKS.tags().createTagKey(desired)) match {
        case tag: ITag[Block] => stacks ++= tag.stream().toList.map(new ItemStack(_))
        case _ =>
      }
    }
    if (stacks.nonEmpty) new ItemStackImageRenderer(stacks.toArray)
    else new TextureImageRenderer(TextureImageProvider.ManualMissingItem) with InteractiveImageRenderer {
      override def getTooltip(tooltip: String): String = "oc:gui.Manual.Warning.OreDictMissing"

      override def onMouseClick(mouseX: Int, mouseY: Int): Boolean = false
    }
  }
}
