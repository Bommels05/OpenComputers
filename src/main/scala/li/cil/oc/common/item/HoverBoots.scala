package li.cil.oc.common.item

import li.cil.oc.Settings
import li.cil.oc.client.renderer.item.HoverBootRenderer
import li.cil.oc.common.init.Items
import li.cil.oc.common.item.data.HoverBootsData
import li.cil.oc.util.ItemColorizer
import net.minecraft.client.model.HumanoidModel
import net.minecraft.core.NonNullList
import net.minecraft.world.effect.{MobEffectInstance, MobEffects}
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.{Entity, EquipmentSlot, LivingEntity}
import net.minecraft.world.item.{ArmorItem, ArmorMaterials, CreativeModeTab, Item, ItemStack}
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{Blocks, CauldronBlock, LayeredCauldronBlock}
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.extensions.IForgeItem

class HoverBoots(props: Properties) extends ArmorItem(ArmorMaterials.DIAMOND, EquipmentSlot.FEET, props) with IForgeItem with traits.SimpleItem with traits.Chargeable {
  override def maxCharge(stack: ItemStack): Double = Settings.get.bufferHoverBoots

  override def getCharge(stack: ItemStack): Double =
    new HoverBootsData(stack).charge

  override def setCharge(stack: ItemStack, amount: Double): Unit = {
    val data = new HoverBootsData(stack)
    data.charge = math.min(maxCharge(stack), math.max(0, amount))
    data.saveData(stack)
  }

  override def canCharge(stack: ItemStack): Boolean = true

  override def charge(stack: ItemStack, amount: Double, simulate: Boolean): Double = {
    val data = new HoverBootsData(stack)
    traits.Chargeable.applyCharge(amount, data.charge, Settings.get.bufferHoverBoots, used => if (!simulate) {
      data.charge += used
      data.saveData(stack)
    })
  }

  override def fillItemCategory(tab: CreativeModeTab, list: NonNullList[ItemStack]): Unit = {
    super.fillItemCategory(tab, list)
    if (allowdedIn(tab)) list.add(Items.createChargedHoverBoots())
  }

  //1.18: Needs to be checked
  /*@OnlyIn(Dist.CLIENT)
  override def getArmorModel[A <: HumanoidModel[_]](entityLiving: LivingEntity, itemStack: ItemStack, armorSlot: EquipmentSlot, _default: A): A = {
    if (armorSlot == slot) {
      HoverBootRenderer.lightColor = if (ItemColorizer.hasColor(itemStack)) ItemColorizer.getColor(itemStack) else 0x66DD55
      HoverBootRenderer.asInstanceOf[A]
    }
    else super.getArmorModel(entityLiving, itemStack, armorSlot, _default)
  }

   */

  override def getArmorTexture(stack: ItemStack, entity: Entity, slot: EquipmentSlot, subType: String): String = {
    if (entity.level.isClientSide) HoverBootRenderer.texture.toString
    else null
  }

  override def onArmorTick(stack: ItemStack, world: Level, player: Player): Unit = {
    super.onArmorTick(stack, world, player)
    if (!Settings.get.ignorePower && player.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null && getCharge(stack) == 0) {
      player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1))
    }
  }

  override def onEntityItemUpdate(stack: ItemStack, entity: ItemEntity): Boolean = {
    if (entity != null && entity.level != null && !entity.level.isClientSide && ItemColorizer.hasColor(stack)) {
      val pos = entity.blockPosition
      val state = entity.level.getBlockState(pos)
      if (state.getBlock.isInstanceOf[LayeredCauldronBlock]) {
        val level = state.getValue(LayeredCauldronBlock.LEVEL).toInt
        if (level > 0) {
          ItemColorizer.removeColor(stack)
          entity.level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, Int.box(level - 1)), 3)
          return true
        }
      }
    }
    super.onEntityItemUpdate(stack, entity)
  }

  override def isBarVisible(stack: ItemStack): Boolean = true

  override def getBarWidth(stack: ItemStack): Int = {
    val data = new HoverBootsData(stack)
    ((1 - data.charge / Settings.get.bufferHoverBoots) * 13).asInstanceOf[Int]
  }

  override def getMaxDamage(stack: ItemStack): Int = Settings.get.bufferHoverBoots.toInt

  // Always show energy bar.
  override def isDamaged(stack: ItemStack): Boolean = true

  // Contradictory as it may seem with the above, this avoids actual damage value changing.
  override def canBeDepleted: Boolean = false

  override def setDamage(stack: ItemStack, damage: Int): Unit = {
    // Subtract energy when taking damage instead of actually damaging the item.
    charge(stack, -damage, simulate = false)

    // Set to 0 for old boots that may have been damaged before.
    super.setDamage(stack, 0)
  }
}
