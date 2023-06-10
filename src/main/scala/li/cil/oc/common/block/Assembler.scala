package li.cil.oc.common.block

import li.cil.oc.Settings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}

class Assembler(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.ASSEMBLER) with traits.PowerAcceptor with traits.StateAware with traits.GUI {
  override def energyThroughput = Settings.get.assemblerRate

  val blockShape = {
    val bottom = Block.box(0, 0, 0, 16, 7, 16)
    val mid = Block.box(2, 7, 2, 14, 9, 14)
    val top = Block.box(0, 9, 0, 16, 16, 16)
    Shapes.or(top, bottom, mid)
  }

  override def getShape(state: BlockState, world: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape = blockShape

  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.Assembler => ContainerTypes.openAssemblerGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Assembler(blockentity.BlockEntityTypes.ASSEMBLER, pos, state)
}
