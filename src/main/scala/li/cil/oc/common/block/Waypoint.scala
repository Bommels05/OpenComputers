package li.cil.oc.common.block

import li.cil.oc.client.gui
import li.cil.oc.common.block.property.PropertyRotatable
import li.cil.oc.common.blockentity
import net.minecraft.client.Minecraft
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.{BlockState, StateDefinition}
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

class Waypoint(props: Properties) extends RedstoneAware(props, blockentity.BlockEntityTypes.WAYPOINT) {
  protected override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]) =
    builder.add(PropertyRotatable.Pitch, PropertyRotatable.Yaw)

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Waypoint(blockentity.BlockEntityTypes.WAYPOINT, pos, state)

  // ----------------------------------------------------------------------- //

  override def use(state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand, trace: BlockHitResult): InteractionResult = {
    if (!player.isCrouching) {
      if (world.isClientSide) world.getBlockEntity(pos) match {
        case t: blockentity.Waypoint => showGui(t)
        case _ =>
      }
      InteractionResult.sidedSuccess(world.isClientSide)
    }
    else super.use(state, world, pos, player, hand, trace)
  }

  @OnlyIn(Dist.CLIENT)
  private def showGui(t: blockentity.Waypoint) {
    Minecraft.getInstance.pushGuiLayer(new gui.Waypoint(t))
  }

  override def getValidRotations(world: Level, pos: BlockPos): Array[Direction] =
    world.getBlockEntity(pos) match {
      case waypoint: blockentity.Waypoint =>
        Direction.values.filter {
          d => d != waypoint.facing && d != waypoint.facing.getOpposite
        }
      case _ => super.getValidRotations(world, pos)
    }
}
