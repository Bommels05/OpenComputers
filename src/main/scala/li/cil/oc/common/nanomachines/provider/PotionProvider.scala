package li.cil.oc.common.nanomachines.provider

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.nanomachines.Behavior
import li.cil.oc.api.nanomachines.DisableReason
import li.cil.oc.api.prefab.AbstractBehavior
import li.cil.oc.common.nanomachines.provider.PotionProvider.PotionBehavior
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.{MobEffect, MobEffectInstance}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.alchemy.Potion
import net.minecraftforge.registries.ForgeRegistries

import scala.collection.convert.ImplicitConversionsToScala._

object PotionProvider extends ScalaProvider("c29e4eec-5a46-479a-9b3d-ad0f06da784a") {
  // Lazy to give other mods a chance to register their potions.
  lazy val PotionWhitelist = filterPotions(Settings.get.nanomachinePotionWhitelist)

  def filterPotions[T](list: Iterable[T]) = {
    list.map {
      case name: String => Option(ForgeRegistries.POTIONS.getValue(new ResourceLocation(name)))
      case loc: ResourceLocation => Option(ForgeRegistries.POTIONS.getValue(loc))
      case id: java.lang.Number => Option(MobEffect.byId(id.intValue()))
      case _ => None
    }.collect {
      case Some(potion) => potion
    }.toSet
  }

  def isPotionEligible(potion: Potion) = potion != null && PotionWhitelist.contains(potion)

  override def createScalaBehaviors(player: Player) = {
    ForgeRegistries.POTIONS.getValues.filter(isPotionEligible).map(potion => new PotionBehavior(potion.getEffects.get(0).getEffect, player))
  }

  override def writeBehaviorToNBT(behavior: Behavior, nbt: CompoundTag): Unit = {
    behavior match {
      case potionBehavior: PotionBehavior =>
        nbt.putString("potionId", potionBehavior.potion.getRegistryName.toString)
      case _ => // Shouldn't happen, ever.
    }
  }

  override def readBehaviorFromNBT(player: Player, nbt: CompoundTag) = {
    val potionId = nbt.getString("potionId")
    //1.18: .get(0) needs to be removed
    new PotionBehavior(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionId)).getEffects.get(0).getEffect, player)
  }

  class PotionBehavior(val potion: MobEffect, player: Player) extends AbstractBehavior(player) {
    final val Duration = 600

    def amplifier(player: Player) = api.Nanomachines.getController(player).getInputCount(this) - 1

    override def getNameHint: String = potion.getDescriptionId.stripPrefix("effect.")

    override def onDisable(reason: DisableReason): Unit = {
      player.removeEffect(potion)
    }

    override def update(): Unit = {
      player.addEffect(new MobEffectInstance(potion, Duration, amplifier(player), true, Settings.get.enableNanomachinePfx))
    }
  }

}
