package li.cil.oc.client.renderer.blockentity

import java.util.function.Function
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.{PoseStack, VertexConsumer}
import com.mojang.math.Vector3f
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.blockentity.Microcontroller
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.{GameRenderer, MultiBufferSource}
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderDispatcher, BlockEntityRenderer}
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation

object MicrocontrollerRenderer extends Function[BlockEntityRenderDispatcher, MicrocontrollerRenderer] {
  override def apply(dispatch: BlockEntityRenderDispatcher) = new MicrocontrollerRenderer(dispatch)
}

class MicrocontrollerRenderer(dispatch: BlockEntityRenderDispatcher) extends BlockEntityRenderer[Microcontroller] {
  override def render(mcu: Microcontroller, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    stack.pushPose()

    stack.translate(0.5, 0.5, 0.5)

    mcu.yaw match {
      case Direction.WEST => stack.mulPose(Vector3f.YP.rotationDegrees(-90))
      case Direction.NORTH => stack.mulPose(Vector3f.YP.rotationDegrees(180))
      case Direction.EAST => stack.mulPose(Vector3f.YP.rotationDegrees(90))
      case _ => // No yaw.
    }

    stack.translate(-0.5, 0.5, 0.505)
    stack.scale(1, -1, 1)

    val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY)

    renderFrontOverlay(stack, Textures.Block.MicrocontrollerFrontLight, r)

    if (mcu.isRunning) {
      renderFrontOverlay(stack, Textures.Block.MicrocontrollerFrontOn, r)
    }
    else if (mcu.hasErrored && RenderUtil.shouldShowErrorLight(mcu.hashCode)) {
      renderFrontOverlay(stack, Textures.Block.MicrocontrollerFrontError, r)
    }

    stack.popPose()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }

  private def renderFrontOverlay(stack: PoseStack, texture: ResourceLocation, r: VertexConsumer): Unit = {
    val icon = Textures.getSprite(texture)
    r.vertex(stack.last.pose, 0, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
    r.vertex(stack.last.pose, 1, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
    r.vertex(stack.last.pose, 1, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
    r.vertex(stack.last.pose, 0, 0, 0).uv(icon.getU0, icon.getV0).endVertex()
  }
}