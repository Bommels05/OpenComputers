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
import net.minecraft.locale.Language
import net.minecraft.world.item.{Item, ItemStack, RecordItem}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.JukeboxBlockEntity

object DriverRecordPlayer extends DriverSidedBlockEntity {
  override def getBlockEntityClass: Class[_] = classOf[JukeboxBlockEntity]

  override def createEnvironment(world: Level, pos: BlockPos, side: Direction): ManagedEnvironment =
    new Environment(world.getBlockEntity(pos).asInstanceOf[JukeboxBlockEntity])

  final class Environment(blockEntity: JukeboxBlockEntity) extends ManagedTileEntityEnvironment[JukeboxBlockEntity](blockEntity, "jukebox") with NamedBlock {
    override def preferredName = "jukebox"

    override def priority = 0

    @Callback(doc = "function():string -- Get the title of the record currently in the jukebox.")
    def getRecord(context: Context, args: Arguments): Array[AnyRef] = {
      val record = blockEntity.getRecord
      if (!record.isEmpty && record.getItem.isInstanceOf[RecordItem]) {
        result(Language.getInstance.getOrDefault(record.getItem.asInstanceOf[RecordItem].getDescriptionId))
      }
      else null
    }

    @Callback(doc = "function() -- Start playing the record currently in the jukebox.")
    def play(context: Context, args: Arguments): Array[AnyRef] = {
      val record = blockEntity.getRecord
      if (!record.isEmpty && record.getItem.isInstanceOf[RecordItem]) {
        blockEntity.getLevel.levelEvent(null, 1010, blockEntity.getBlockPos, Item.getId(record.getItem))
        result(true)
      }
      else null
    }

    @Callback(doc = "function() -- Stop playing the record currently in the jukebox.")
    def stop(context: Context, args: Arguments): Array[AnyRef] = {
      blockEntity.getLevel.levelEvent(1010, blockEntity.getBlockPos, 0)
      null
    }
  }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] = {
      if (stack.getItem == Blocks.JUKEBOX.asItem)
        classOf[Environment]
      else null
    }
  }

}
