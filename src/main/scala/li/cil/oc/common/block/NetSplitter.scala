package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import li.cil.oc.integration.util.Wrench
import net.minecraft.core.BlockPos
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class NetSplitter(props: Properties) extends RedstoneAware(props, blockentity.BlockEntityTypes.NET_SPLITTER) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.NetSplitter(blockentity.BlockEntityTypes.NET_SPLITTER, pos, state)

  // ----------------------------------------------------------------------- //

  // NOTE: must not be final for immibis microblocks to work.
  override def use(state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand, trace: BlockHitResult): InteractionResult = {
    if (Wrench.holdsApplicableWrench(player, pos)) {
      val side = trace.getDirection
      val sideToToggle = if (player.isCrouching) side.getOpposite else side
      world.getBlockEntity(pos) match {
        case splitter: blockentity.NetSplitter =>
          if (!world.isClientSide) {
            val oldValue = splitter.openSides(sideToToggle.ordinal())
            splitter.setSideOpen(sideToToggle, !oldValue)
          }
          InteractionResult.sidedSuccess(world.isClientSide)
        case _ => InteractionResult.PASS
      }
    }
    else super.use(state, world, pos, player, hand, trace)
  }
}
