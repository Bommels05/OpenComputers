package li.cil.oc.common.blockentity

import li.cil.oc.api
import li.cil.oc.api.network.Visibility
import li.cil.oc.common
import li.cil.oc.Constants
import li.cil.oc.util.Color
import li.cil.oc.util.ItemColorizer
import net.minecraft.core.BlockPos
import net.minecraft.world.item.{DyeColor, ItemStack}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

class Cable(selfType: BlockEntityType[_ <: Cable], pos: BlockPos, state: BlockState) extends BlockEntity(selfType, pos, state) with traits.Environment with traits.NotAnalyzable with traits.ImmibisMicroblock with traits.Colored {
  val node = api.Network.newNode(this, Visibility.None).create()

  setColor(Color.rgbValues(DyeColor.LIGHT_GRAY))

  def createItemStack() = {
    val stack = api.Items.get(Constants.BlockName.Cable).createItemStack(1)
    if (getColor != Color.rgbValues(DyeColor.LIGHT_GRAY)) {
      ItemColorizer.setColor(stack, getColor)
    }
    stack
  }

  def fromItemStack(stack: ItemStack): Unit = {
    if (ItemColorizer.hasColor(stack)) {
      setColor(ItemColorizer.getColor(stack))
    }
  }

  override def controlsConnectivity = true

  override def consumesDye = true

  override protected def onColorChanged() {
    super.onColorChanged()
    if (getLevel != null && isServer) {
      api.Network.joinOrCreateNetwork(this)
    }
  }
}