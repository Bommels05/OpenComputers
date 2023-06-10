package li.cil.oc.common.block

import li.cil.oc.Settings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class Relay(props: Properties) extends SimpleBlock(props, blockentity.BlockEntityTypes.RELAY) with traits.GUI with traits.PowerAcceptor {
  override def openGui(player: ServerPlayer, world: Level, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: blockentity.Relay => ContainerTypes.openRelayGui(player, te)
    case _ =>
  }

  override def energyThroughput = Settings.get.accessPointRate

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Relay(blockentity.BlockEntityTypes.RELAY, pos, state)
}
