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

object DisassemblerRenderer extends Function[BlockEntityRenderDispatcher, DisassemblerRenderer] {
  override def apply(dispatch: BlockEntityRenderDispatcher) = new DisassemblerRenderer(dispatch)
}

class DisassemblerRenderer(dispatch: BlockEntityRenderDispatcher) extends BlockEntityRenderer[blockentity.Disassembler] {
  override def render(disassembler: blockentity.Disassembler, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    if (disassembler.isActive) {
      stack.pushPose()

      stack.translate(0.5, 0.5, 0.5)
      stack.scale(1.0025f, -1.0025f, 1.0025f)
      stack.translate(-0.5f, -0.5f, -0.5f)

      val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY)

      {
        val icon = Textures.getSprite(Textures.Block.DisassemblerTopOn)
        r.vertex(stack.last.pose, 0, 0, 1).uv(icon.getU0, icon.getV1).endVertex()
        r.vertex(stack.last.pose, 1, 0, 1).uv(icon.getU1, icon.getV1).endVertex()
        r.vertex(stack.last.pose, 1, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
        r.vertex(stack.last.pose, 0, 0, 0).uv(icon.getU0, icon.getV0).endVertex()
      }

      {
        val icon = Textures.getSprite(Textures.Block.DisassemblerSideOn)
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
      }

      stack.popPose()
    }

    RenderState.checkError(getClass.getName + ".render: leaving")
  }

}
