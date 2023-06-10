package li.cil.oc.common.capabilities

import li.cil.oc.api.network.Node
import li.cil.oc.api.network.SidedEnvironment
import li.cil.oc.integration.Mods
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.common.util.NonNullSupplier

object CapabilitySidedEnvironment {
  final val ProviderSidedEnvironment = new ResourceLocation(Mods.IDs.OpenComputers, "sided_environment")

  class Provider(val blockEntity: BlockEntity with SidedEnvironment) extends ICapabilityProvider with NonNullSupplier[Provider] with SidedEnvironment {
    private val wrapper = LazyOptional.of(this)

    def get = this

    def invalidate() = wrapper.invalidate

    override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
      if (capability == Capabilities.SidedEnvironmentCapability) wrapper.cast[T]
      else LazyOptional.empty[T]
    }

    override def sidedNode(side: Direction) = blockEntity.sidedNode(side)

    override def canConnect(side: Direction) = blockEntity.canConnect(side)
  }

  class DefaultImpl extends SidedEnvironment {
    override def sidedNode(side: Direction): Node = null

    override def canConnect(side: Direction): Boolean = false
  }
}
