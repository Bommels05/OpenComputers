package li.cil.oc.client.renderer.blockentity

import java.util.function.Function
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import li.cil.oc.api.event.RackMountableRenderEvent
import li.cil.oc.api.event.RackMountableRenderEvent.BlockEntity
import li.cil.oc.common.blockentity.Rack
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.{GameRenderer, MultiBufferSource}
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderDispatcher, BlockEntityRenderer}
import net.minecraft.core.Direction
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.opengl.GL11

object RackRenderer extends Function[BlockEntityRenderDispatcher, RackRenderer] {
  override def apply(dispatch: BlockEntityRenderDispatcher) = new RackRenderer(dispatch)
}

class RackRenderer(dispatch: BlockEntityRenderDispatcher) extends BlockEntityRenderer[Rack] {
  private final val vOffset = 2 / 16f
  private final val vSize = 3 / 16f

  override def render(rack: Rack, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    stack.pushPose()

    stack.translate(0.5, 0.5, 0.5)

    rack.yaw match {
      case Direction.WEST => stack.mulPose(Vector3f.YP.rotationDegrees(-90))
      case Direction.NORTH => stack.mulPose(Vector3f.YP.rotationDegrees(180))
      case Direction.EAST => stack.mulPose(Vector3f.YP.rotationDegrees(90))
      case _ => // No yaw.
    }

    stack.translate(-0.5, 0.5, 0.505 - 0.5f / 16f)
    stack.scale(1, -1, 1)

    // Note: we manually sync the rack inventory for this to work.
    for (i <- 0 until rack.getContainerSize) {
      if (!rack.getItem(i).isEmpty) {
        val v0 = vOffset + i * vSize
        val v1 = vOffset + (i + 1) * vSize
        val event = new BlockEntity(rack, i, rack.lastData(i), stack, buffer, light, overlay, v0, v1)
        MinecraftForge.EVENT_BUS.post(event)
      }
    }

    stack.popPose()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }
}