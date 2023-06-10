package li.cil.oc.common.block

import net.minecraft.core.{BlockPos, NonNullList}
import net.minecraft.world.item.context.BlockPlaceContext

import java.util.List
import net.minecraft.world.item.{CreativeModeTab, DyeColor, ItemStack}
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.{BlockState, StateDefinition}
import net.minecraft.world.level.block.state.properties.EnumProperty

object ChameliumBlock {
  final val Color = EnumProperty.create("color", classOf[DyeColor])
}

class ChameliumBlock(props: Properties) extends SimpleBlock(props, null) {
  protected override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = {
    builder.add(ChameliumBlock.Color)
  }
  registerDefaultState(stateDefinition.any.setValue(ChameliumBlock.Color, DyeColor.BLACK))

  override def getCloneItemStack(world: BlockGetter, pos: BlockPos, state: BlockState): ItemStack = {
    val stack = new ItemStack(this)
    stack.setDamageValue(state.getValue(ChameliumBlock.Color).getId)
    stack
  }

  override def getStateForPlacement(ctx: BlockPlaceContext): BlockState =
    defaultBlockState.setValue(ChameliumBlock.Color, DyeColor.byId(ctx.getItemInHand.getDamageValue))

  override def fillItemCategory(tab: CreativeModeTab, list: NonNullList[ItemStack]) {
    val stack = new ItemStack(this, 1)
    stack.setDamageValue(defaultBlockState.getValue(ChameliumBlock.Color).getId)
    list.add(stack)
  }
}
