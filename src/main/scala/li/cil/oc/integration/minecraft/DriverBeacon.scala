package li.cil.oc.integration.minecraft

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.NamedBlock
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.ManagedEnvironment
import li.cil.oc.api.prefab.DriverSidedBlockEntity
import li.cil.oc.integration.ManagedTileEntityEnvironment
import li.cil.oc.util.ResultWrapper.result
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{Block, Blocks}
import net.minecraft.world.level.block.entity.BeaconBlockEntity

object DriverBeacon extends DriverSidedBlockEntity {
  override def getBlockEntityClass: Class[_] = classOf[BeaconBlockEntity]

  override def createEnvironment(world: Level, pos: BlockPos, side: Direction): ManagedEnvironment =
    new Environment(world.getBlockEntity(pos).asInstanceOf[BeaconBlockEntity])

  final class Environment(blockEntity: BeaconBlockEntity) extends ManagedTileEntityEnvironment[BeaconBlockEntity](blockEntity, "beacon") with NamedBlock {
    override def preferredName = "beacon"

    override def priority = 0

    @Callback(doc = "function():number -- Get the number of levels for this beacon.")
    def getLevels(context: Context, args: Arguments): Array[AnyRef] = {
      result(blockEntity.levels)
    }

    @Callback(doc = "function():string -- Get the name of the active primary effect.")
    def getPrimaryEffect(context: Context, args: Arguments): Array[AnyRef] = {
      result(getEffectName(blockEntity.primaryPower))
    }

    @Callback(doc = "function():string -- Get the name of the active secondary effect.")
    def getSecondaryEffect(context: Context, args: Arguments): Array[AnyRef] = {
      result(getEffectName(blockEntity.secondaryPower))
    }

    private def getEffectName(effect: MobEffect): String = {
      if (effect != null) effect.getRegistryName.toString else null
    }
  }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] = {
      if (!stack.isEmpty && Block.byItem(stack.getItem) == Blocks.BEACON)
        classOf[Environment]
      else null
    }
  }

}
