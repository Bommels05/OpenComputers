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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{Block, Blocks}
import net.minecraft.world.level.block.entity.CommandBlockEntity
import net.minecraftforge.server.ServerLifecycleHooks

object DriverCommandBlock extends DriverSidedBlockEntity {
  override def getBlockEntityClass: Class[_] = classOf[CommandBlockEntity]

  override def createEnvironment(world: Level, pos: BlockPos, side: Direction): ManagedEnvironment =
    new Environment(world.getBlockEntity(pos).asInstanceOf[CommandBlockEntity])

  final class Environment(blockEntity: CommandBlockEntity) extends ManagedTileEntityEnvironment[CommandBlockEntity](blockEntity, "command_block") with NamedBlock {
    override def preferredName = "command_block"

    override def priority = 0

    @Callback(direct = true, doc = "function():string -- Get the command currently set in this command block.")
    def getCommand(context: Context, args: Arguments): Array[AnyRef] = {
      result(blockEntity.getCommandBlock.getCommand)
    }

    @Callback(doc = "function(value:string) -- Set the specified command for the command block.")
    def setCommand(context: Context, args: Arguments): Array[AnyRef] = {
      blockEntity.getCommandBlock.setCommand(args.checkString(0))
      blockEntity.getLevel.sendBlockUpdated(blockEntity.getBlockPos, blockEntity.getLevel.getBlockState(blockEntity.getBlockPos), blockEntity.getLevel.getBlockState(blockEntity.getBlockPos), 3)
      result(true)
    }

    @Callback(doc = "function():number -- Execute the currently set command. This has a slight delay to allow the command block to properly update.")
    def executeCommand(context: Context, args: Arguments): Array[AnyRef] = {
      context.pause(0.1)
      if (!ServerLifecycleHooks.getCurrentServer.isCommandBlockEnabled) {
        result(null, "command blocks are disabled")
      } else {
        val commandSender = blockEntity.getCommandBlock
        commandSender.performCommand(blockEntity.getLevel)
        result(commandSender.getSuccessCount, commandSender.getLastOutput.getString)
      }
    }
  }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] = {
      if (!stack.isEmpty && Block.byItem(stack.getItem) == Blocks.COMMAND_BLOCK)
        classOf[Environment]
      else null
    }
  }

}
