package li.cil.oc.common.block

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.common.block
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.common.item.data.RobotData
import li.cil.oc.common.blockentity
import li.cil.oc.util.Rarity
import net.minecraft.core.Direction
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.{BlockItem, Item, ItemStack}
import net.minecraft.world.item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class Item(value: Block, props: Item.Properties) extends BlockItem(value, props) {
  override def getRarity(stack: ItemStack): item.Rarity = getBlock match {
    case _: block.Microcontroller => {
      val data = new MicrocontrollerData(stack)
      Rarity.byTier(data.tier)
    }
    case _: block.RobotProxy => {
      val data = new RobotData(stack)
      Rarity.byTier(data.tier)
    }
    case _ => super.getRarity(stack)
  }

  override def getName(stack: ItemStack): Component = {
    if (api.Items.get(stack) == api.Items.get(Constants.BlockName.Print)) {
      val data = new PrintData(stack)
      data.label.map(new TextComponent(_)).getOrElse(super.getName(stack))
    }
    else super.getName(stack)
  }

  @Deprecated
  override def getDescriptionId: String = getBlock match {
    case simple: SimpleBlock => simple.getDescriptionId
    case _ => Settings.namespace + "tile"
  }

  override def placeBlock(ctx: BlockPlaceContext, newState: BlockState): Boolean = {
    // When placing robots in creative mode, we have to copy the stack
    // manually before it's placed to ensure different component addresses
    // in the different robots, to avoid interference of screens e.g.
    val needsCopying = ctx.getPlayer.isCreative && api.Items.get(ctx.getItemInHand) == api.Items.get(Constants.BlockName.Robot)
    val ctxToUse = if (needsCopying) {
      val stackToUse = new RobotData(ctx.getItemInHand).copyItemStack()
      val hitResult = new BlockHitResult(ctx.getClickLocation, ctx.getClickedFace, ctx.getClickedPos, ctx.isInside)
      new BlockPlaceContext(ctx.getLevel, ctx.getPlayer, ctx.getHand, stackToUse, hitResult)
    }
    else ctx
    if (super.placeBlock(ctxToUse, newState)) {
      // If it's a rotatable block try to make it face the player.
      ctx.getLevel.getBlockEntity(ctxToUse.getClickedPos) match {
        case keyboard: blockentity.Keyboard => // Ignore.
        case rotatable: blockentity.traits.Rotatable =>
          rotatable.setFromEntityPitchAndYaw(ctxToUse.getPlayer)
          if (!rotatable.validFacings.contains(rotatable.pitch)) {
            rotatable.pitch = rotatable.validFacings.headOption.getOrElse(Direction.NORTH)
          }
          if (!rotatable.isInstanceOf[blockentity.RobotProxy]) {
            rotatable.invertRotation()
          }
        case _ => // Ignore.
      }
      true
    }
    else false
  }
}
