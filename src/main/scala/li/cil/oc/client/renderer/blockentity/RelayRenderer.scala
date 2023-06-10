package li.cil.oc.client.renderer.blockentity

import java.util.function.Function
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.blockentity
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.{GameRenderer, MultiBufferSource}
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderDispatcher, BlockEntityRenderer}

object RelayRenderer extends Function[BlockEntityRenderDispatcher, RelayRenderer] {
  override def apply(dispatch: BlockEntityRenderDispatcher) = new RelayRenderer(dispatch)
}

class RelayRenderer(dispatch: BlockEntityRenderDispatcher) extends BlockEntityRenderer[blockentity.Relay] {
  override def render(switch: blockentity.Relay, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    val activity = math.max(0, 1 - (System.currentTimeMillis() - switch.lastMessage) / 1000.0)
    if (activity > 0) {
      stack.pushPose()

      stack.translate(0.5, 0.5, 0.5)
      stack.scale(1.0025f, -1.0025f, 1.0025f)
      stack.translate(-0.5f, -0.5f, -0.5f)

      val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY)

      val icon = Textures.getSprite(Textures.Block.SwitchSideOn)
      r.vertex(stack.last.pose, 1, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).uv(icon.getU0, icon.getV0).endVertex()

      r.vertex(stack.last.pose, 0, 1, 1).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 1, 1).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).uv(icon.getU0, icon.getV0).endVertex()

      r.vertex(stack.last.pose, 1, 1, 1).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).uv(icon.getU0, icon.getV0).endVertex()

      r.vertex(stack.last.pose, 0, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 1).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 0, 0, 0).uv(icon.getU0, icon.getV0).endVertex()

      stack.popPose()
    }

    RenderState.checkError(getClass.getName + ".render: leaving")
  }
}
