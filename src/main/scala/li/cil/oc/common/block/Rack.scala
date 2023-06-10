package li.cil.oc.common.block

import li.cil.oc.Settings
import li.cil.oc.api.component.RackMountable
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.block.property.PropertyRotatable
import li.cil.oc.common.blockentity
import net.minecraft.core.Direction.Axis
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.{BlockState, StateDefinition}
import net.minecraft.world.phys.Vec3

class Rack(props: Properties) extends RedstoneAware(props, blockentity.BlockEntityTypes.RACK) with traits.PowerAcceptor with traits.StateAware with traits.GUI {
  protected override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]) =
    builder.add(PropertyRotatable.Facing)

  // ----------------------------------------------------------------------- //

  override def energyThroughput = Settings.get.serverRackRate

  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.Rack => ContainerTypes.openRackGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Rack(blockentity.BlockEntityTypes.RACK, pos, state)

  // ----------------------------------------------------------------------- //

  override def localOnBlockActivated(world: Level, pos: BlockPos, player: Player, hand: InteractionHand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    world.getBlockEntity(pos) match {
      case rack: blockentity.Rack => rack.slotAt(side, hitX, hitY, hitZ) match {
        case Some(slot) =>
          // Snap to grid to get same behavior on client and server...
          val hitVec = new Vec3((hitX * 16f).toInt / 16f, (hitY * 16f).toInt / 16f, (hitZ * 16f).toInt / 16f)
          val rotation = side match {
            case Direction.WEST => Math.toRadians(90).toFloat
            case Direction.NORTH => Math.toRadians(180).toFloat
            case Direction.EAST => Math.toRadians(270).toFloat
            case _ => 0
          }
          // Rotate *centers* of pixels to keep association when reversing axis.
          val localHitVec = rotate(hitVec.add(-0.5 + 1 / 32f, -0.5 + 1 / 32f, -0.5 + 1 / 32f), rotation).add(0.5 - 1 / 32f, 0.5 - 1 / 32f, 0.5 - 1 / 32f)
          val globalX = (localHitVec.x * 16.05f).toInt // [0, 15], work around floating point inaccuracies
          val globalY = (localHitVec.y * 16.05f).toInt // [0, 15], work around floating point inaccuracies
          val localX = (if (side.getAxis != Axis.Z) 15 - globalX else globalX) - 1
          val localY = (15 - globalY) - 2 - 3 * slot
          if (localX >= 0 && localX < 14 && localY >= 0 && localY < 3) rack.getMountable(slot) match {
            case mountable: RackMountable if mountable.onActivate(player, hand, heldItem, localX / 14f, localY / 3f) => return true // Activation handled by mountable.
            case _ =>
          }
        case _ =>
      }
      case _ =>
    }
    super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
  }

  def rotate(v: Vec3, t: Float): Vec3 = {
    val cos = Math.cos(t)
    val sin = Math.sin(t)
    new Vec3(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos)
  }
}
