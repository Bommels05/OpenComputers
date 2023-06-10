package li.cil.oc.common.block

import java.util.Random
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.common.blockentity
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.{BlockHitResult, HitResult}
import net.minecraft.world.phys.shapes.{CollisionContext, VoxelShape}

class RobotAfterimage(props: Properties) extends SimpleBlock(props, null) {
  override def getCloneItemStack(state: BlockState, target: HitResult, world: BlockGetter, pos: BlockPos, player: Player): ItemStack =
    findMovingRobot(world, pos) match {
      case Some(robot) => robot.info.createItemStack()
      case _ => ItemStack.EMPTY
    }

  override def getShape(state: BlockState, world: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape = {
    findMovingRobot(world, pos) match {
      case Some(robot) =>
        val block = robot.getBlockState.getBlock.asInstanceOf[SimpleBlock]
        val shape = block.getShape(state, world, robot.getBlockPos, ctx)
        val delta = robot.moveFrom.fold(BlockPos.ZERO)(vec => {
          val blockPos = robot.getBlockPos
          new BlockPos(blockPos.getX - vec.getX, blockPos.getY - vec.getY, blockPos.getZ - vec.getZ)
        })
        shape.move(delta.getX, delta.getY, delta.getZ)
      case _ => super.getShape(state, world, pos, ctx)
    }
  }

  // ----------------------------------------------------------------------- //

  override def onPlace(state: BlockState, world: Level, pos: BlockPos, prevState: BlockState, moved: Boolean): Unit = {
    if (!world.isClientSide) {
      world.asInstanceOf[ServerLevel].scheduleTick(pos, this, Math.max((Settings.get.moveDelay * 20).toInt, 1) - 1)
    }
  }

  override def tick(state: BlockState, world: ServerLevel, pos: BlockPos, rand: Random) {
    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState)
  }

  override def onDestroyedByPlayer(state: BlockState, world: Level, pos: BlockPos, player: Player, willHarvest: Boolean, fluid: FluidState): Boolean = {
    findMovingRobot(world, pos) match {
      case Some(robot) if robot.isAnimatingMove && robot.moveFrom.contains(pos) =>
        robot.proxy.getBlockState.getBlock.onDestroyedByPlayer(state, world, pos, player, false, fluid)
      case _ => super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid) // Probably broken by the robot we represent.
    }
  }

  @Deprecated
  override def use(state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand, trace: BlockHitResult): InteractionResult = {
    findMovingRobot(world, pos) match {
      case Some(robot) => api.Items.get(Constants.BlockName.Robot).block.use(world.getBlockState(robot.getBlockPos), world, robot.getBlockPos, player, hand, trace)
      case _ => if (world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState)) InteractionResult.sidedSuccess(world.isClientSide) else InteractionResult.PASS
    }
  }

  def findMovingRobot(world: BlockGetter, pos: BlockPos): Option[blockentity.Robot] = {
    for (side <- Direction.values) {
      val tpos: BlockPos = pos.relative(side)
      if (world match {
        case world: Level => world.isLoaded(tpos)
        case _ => true
      }) world.getBlockEntity(tpos) match {
        case proxy: blockentity.RobotProxy if proxy.robot.moveFrom.contains(pos) => return Some(proxy.robot)
        case _ =>
      }
    }
    None
  }
}
