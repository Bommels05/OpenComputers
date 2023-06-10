package li.cil.oc.common.block

import li.cil.oc.common.block.property.PropertyCableConnection
import li.cil.oc.common.capabilities.Capabilities
import li.cil.oc.common.blockentity
import li.cil.oc.util.{Color, ItemColorizer}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.{DyeColor, ItemStack}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.{BlockState, StateDefinition}
import net.minecraft.world.level.{BlockGetter, Level, LevelAccessor}
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}
import net.minecraftforge.common.extensions.IForgeBlock

class Cable(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.CABLE) with IForgeBlock {
  // For Immibis Microblock support.
  val ImmibisMicroblocks_TransformableBlockMarker = null

  // For FMP part coloring.
  var colorMultiplierOverride: Option[Int] = None

  // ----------------------------------------------------------------------- //

  override protected def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]) = {
    builder.add(PropertyCableConnection.DOWN, PropertyCableConnection.UP,
      PropertyCableConnection.NORTH, PropertyCableConnection.SOUTH,
      PropertyCableConnection.WEST, PropertyCableConnection.EAST)
  }

  registerDefaultState(defaultBlockState.
    setValue(PropertyCableConnection.DOWN, PropertyCableConnection.Shape.NONE).
    setValue(PropertyCableConnection.UP, PropertyCableConnection.Shape.NONE).
    setValue(PropertyCableConnection.NORTH, PropertyCableConnection.Shape.NONE).
    setValue(PropertyCableConnection.SOUTH, PropertyCableConnection.Shape.NONE).
    setValue(PropertyCableConnection.WEST, PropertyCableConnection.Shape.NONE).
    setValue(PropertyCableConnection.EAST, PropertyCableConnection.Shape.NONE))

  override def getStateForPlacement(ctx: BlockPlaceContext): BlockState = {
    val color = Cable.getConnectionColor(ctx.getItemInHand)
    val fromPos = new BlockPos.MutableBlockPos()
    Direction.values.foldLeft(defaultBlockState)((state, fromSide) => {
      fromPos.setWithOffset(ctx.getClickedPos, fromSide)
      val fromState = ctx.getLevel.getBlockState(fromPos)
      Cable.updateState(state, null, color, fromSide, fromState, ctx.getLevel, fromPos)
    })
  }

  override def getCloneItemStack(state: BlockState, target: HitResult, world: BlockGetter, pos: BlockPos, player: Player) =
    world.getBlockEntity(pos) match {
      case t: blockentity.Cable => t.createItemStack()
      case _ => createItemStack()
    }

  override def getShape(state: BlockState, world: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape = Cable.shape(state)

  override def neighborChanged(state: BlockState, world: Level, pos: BlockPos, other: Block, otherPos: BlockPos, moved: Boolean): Unit = {
    if (world.isClientSide) return
    val newState = world.getBlockEntity(pos) match {
      case t: blockentity.Cable => {
        val fromPos = new BlockPos.MutableBlockPos()
        Direction.values.foldLeft(state)((state, fromSide) => {
          fromPos.setWithOffset(pos, fromSide)
          val fromState = world.getBlockState(fromPos)
          Cable.updateState(state, t, -1, fromSide, fromState, world, fromPos)
        })
      }
      case _ => state
    }
    if (newState != state) world.setBlock(pos, newState, 0x13)
  }

  override def updateShape(state: BlockState, fromSide: Direction, fromState: BlockState, world: LevelAccessor, pos: BlockPos, fromPos: BlockPos): BlockState =
    Cable.updateState(state, world.getBlockEntity(pos), -1, fromSide, fromState, world, fromPos)

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Cable(blockentity.BlockEntityTypes.CABLE, pos, state)

  // ----------------------------------------------------------------------- //

  override def setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack): Unit = {
    super.setPlacedBy(world, pos, state, placer, stack)
    world.getBlockEntity(pos) match {
      case tileEntity: blockentity.Cable => {
        tileEntity.fromItemStack(stack)
        state.updateNeighbourShapes(world, pos, 2)
      }
      case _ =>
    }
  }
}

object Cable {
  final val MIN = 0.375
  final val MAX = 1 - MIN

  final val DefaultShape: VoxelShape = Shapes.box(MIN, MIN, MIN, MAX, MAX, MAX)

  final val CachedParts: Array[VoxelShape] = Array(
    Shapes.box( MIN, 0, MIN, MAX, MIN, MAX ), // Down
    Shapes.box( MIN, MAX, MIN, MAX, 1, MAX ), // Up
    Shapes.box( MIN, MIN, 0, MAX, MAX, MIN ), // North
    Shapes.box( MIN, MIN, MAX, MAX, MAX, 1 ), // South
    Shapes.box( 0, MIN, MIN, MIN, MAX, MAX ), // West
    Shapes.box( MAX, MIN, MIN, 1, MAX, MAX )) // East

  final val CachedBounds = {
    // 6 directions = 6 bits = 11111111b >> 2 = 0xFF >> 2
    (0 to 0xFF >> 2).map(mask => {
      Direction.values.foldLeft(DefaultShape)((shape, side) => {
        if (((1 << side.get3DDataValue) & mask) != 0) Shapes.or(shape, CachedParts(side.ordinal()))
        else shape
      })
    }).toArray
  }

  def mask(side: Direction, value: Int = 0) = value | (1 << side.get3DDataValue)

  def shape(state: BlockState): VoxelShape = {
    var result = 0
    for (side <- Direction.values) {
      val sideShape = state.getValue(PropertyCableConnection.BY_DIRECTION.get(side))
      if (sideShape != PropertyCableConnection.Shape.NONE) {
        result = mask(side, result)
      }
    }
    Cable.CachedBounds(result)
  }

  def updateState(state: BlockState, blockEntity: BlockEntity, defaultColor: Int, fromSide: Direction, fromState: BlockState, world: BlockGetter, fromPos: BlockPos): BlockState = {
    val prop = PropertyCableConnection.BY_DIRECTION.get(fromSide)
    val neighborTileEntity = world.getBlockEntity(fromPos)
    if (neighborTileEntity != null && neighborTileEntity.getLevel != null) {
      val neighborHasNode = hasNetworkNode(neighborTileEntity, fromSide.getOpposite)
      val canConnectColor = canConnectBasedOnColor(blockEntity, neighborTileEntity, defaultColor)
      val canConnectIM = canConnectFromSideIM(blockEntity, fromSide) && canConnectFromSideIM(neighborTileEntity, fromSide.getOpposite)
      if (neighborHasNode && canConnectColor && canConnectIM) {
        if (fromState.is(state.getBlock)) {
          return state.setValue(prop, PropertyCableConnection.Shape.CABLE)
        }
        else {
          return state.setValue(prop, PropertyCableConnection.Shape.DEVICE)
        }
      }
    }
    state.setValue(prop, PropertyCableConnection.Shape.NONE)
  }

  private def hasNetworkNode(blockEntity: BlockEntity, side: Direction): Boolean = {
    if (blockEntity != null) {
      if (blockEntity.isInstanceOf[blockentity.RobotProxy]) return false

      if (blockEntity.getCapability(Capabilities.SidedEnvironmentCapability, side).isPresent) {
        val host = blockEntity.getCapability(Capabilities.SidedEnvironmentCapability, side).orElse(null)
        if (host != null) {
          return if (blockEntity.getLevel.isClientSide) host.canConnect(side) else host.sidedNode(side) != null
        }
      }

      if (blockEntity.getCapability(Capabilities.EnvironmentCapability, side).isPresent) {
        val host = blockEntity.getCapability(Capabilities.EnvironmentCapability, side)
        if (host.isPresent) return true
      }
    }

    false
  }

  private def getConnectionColor(stack: ItemStack): Int = {
    val color = ItemColorizer.getColor(stack)
    if (color == -1) Color.rgbValues(DyeColor.LIGHT_GRAY) else color
  }

  private def getConnectionColor(blockEntity: BlockEntity): Int = {
    if (blockEntity != null) {
      if (blockEntity.getCapability(Capabilities.ColoredCapability, null).isPresent) {
        val colored = blockEntity.getCapability(Capabilities.ColoredCapability, null).orElse(null)
        if (colored != null && colored.controlsConnectivity) return colored.getColor
      }
    }

    Color.rgbValues(DyeColor.LIGHT_GRAY)
  }

  private def canConnectBasedOnColor(be1: BlockEntity, be2: BlockEntity, c1Default: Int = Color.rgbValues(DyeColor.LIGHT_GRAY)) = {
    val (c1, c2) = (if (be1 == null) c1Default else getConnectionColor(be1), getConnectionColor(be2))
    c1 == c2 || c1 == Color.rgbValues(DyeColor.LIGHT_GRAY) || c2 == Color.rgbValues(DyeColor.LIGHT_GRAY)
  }

  private def canConnectFromSideIM(blockEntity: BlockEntity, side: Direction) =
    blockEntity match {
      case im: blockentity.traits.ImmibisMicroblock => im.ImmibisMicroblocks_isSideOpen(side.ordinal)
      case _ => true
    }
}
