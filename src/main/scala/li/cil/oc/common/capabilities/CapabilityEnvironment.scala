package li.cil.oc.common.capabilities

import li.cil.oc.api
import li.cil.oc.api.network.Environment
import li.cil.oc.api.network.Message
import li.cil.oc.api.network.Node
import li.cil.oc.api.network.Visibility
import li.cil.oc.integration.Mods
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.common.util.NonNullSupplier

object CapabilityEnvironment {
  final val ProviderEnvironment = new ResourceLocation(Mods.IDs.OpenComputers, "environment")

  class Provider(val blockEntity: BlockEntity with Environment) extends ICapabilityProvider with NonNullSupplier[Provider] with Environment {
    private val wrapper = LazyOptional.of(this)

    def get = this

    def invalidate() = wrapper.invalidate

    override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
      if (capability == Capabilities.EnvironmentCapability) wrapper.cast[T]
      else LazyOptional.empty[T]
    }

    override def node = blockEntity.node

    override def onMessage(message: Message) = blockEntity.onMessage(message)

    override def onConnect(node: Node) = blockEntity.onConnect(node)

    override def onDisconnect(node: Node) = blockEntity.onDisconnect(node)
  }

  class DefaultImpl extends Environment {
    override val node = api.Network.newNode(this, Visibility.None).create()

    override def onMessage(message: Message): Unit = {}

    override def onConnect(node: Node): Unit = {}

    override def onDisconnect(node: Node): Unit = {}
  }
}
