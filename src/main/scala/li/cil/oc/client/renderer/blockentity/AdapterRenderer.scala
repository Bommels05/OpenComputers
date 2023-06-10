package li.cil.oc.client.renderer.blockentity

import java.util.function.Function
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.blockentity
import li.cil.oc.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{GameRenderer, MultiBufferSource}
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderDispatcher, BlockEntityRenderer}
import net.minecraft.core.Direction

object AdapterRenderer extends Function[BlockEntityRenderDispatcher, AdapterRenderer] {
  override def apply(dispatch: BlockEntityRenderDispatcher) = new AdapterRenderer(dispatch)
}

class AdapterRenderer(dispatch: BlockEntityRenderDispatcher) extends BlockEntityRenderer[blockentity.Adapter] {
  override def render(adapter: blockentity.Adapter, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    if (adapter.openSides.contains(true)) {
      stack.pushPose()

      stack.translate(0.5, 0.5, 0.5)
      stack.scale(1.0025f, -1.0025f, 1.0025f)
      stack.translate(-0.5f, -0.5f, -0.5f)

      val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY)

      val sideActivity = Textures.getSprite(Textures.Block.AdapterOn)

      if (adapter.isSideOpen(Direction.DOWN)) {
        r.vertex(stack.last.pose, 0, 1, 0).uv(sideActivity.getU1, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 1, 1, 0).uv(sideActivity.getU0, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 1, 1, 1).uv(sideActivity.getU0, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 0, 1, 1).uv(sideActivity.getU1, sideActivity.getV1).endVertex()
      }

      if (adapter.isSideOpen(Direction.UP)) {
        r.vertex(stack.last.pose, 0, 0, 0).uv(sideActivity.getU1, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 0, 0, 1).uv(sideActivity.getU1, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 1, 0, 1).uv(sideActivity.getU0, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 1, 0, 0).uv(sideActivity.getU0, sideActivity.getV1).endVertex()
      }

      if (adapter.isSideOpen(Direction.NORTH)) {
        r.vertex(stack.last.pose, 1, 1, 0).uv(sideActivity.getU0, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 0, 1, 0).uv(sideActivity.getU1, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 0, 0, 0).uv(sideActivity.getU1, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 1, 0, 0).uv(sideActivity.getU0, sideActivity.getV0).endVertex()
      }

      if (adapter.isSideOpen(Direction.SOUTH)) {
        r.vertex(stack.last.pose, 0, 1, 1).uv(sideActivity.getU0, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 1, 1, 1).uv(sideActivity.getU1, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 1, 0, 1).uv(sideActivity.getU1, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 0, 0, 1).uv(sideActivity.getU0, sideActivity.getV0).endVertex()
      }

      if (adapter.isSideOpen(Direction.WEST)) {
        r.vertex(stack.last.pose, 0, 1, 0).uv(sideActivity.getU0, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 0, 1, 1).uv(sideActivity.getU1, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 0, 0, 1).uv(sideActivity.getU1, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 0, 0, 0).uv(sideActivity.getU0, sideActivity.getV0).endVertex()
      }

      if (adapter.isSideOpen(Direction.EAST)) {
        r.vertex(stack.last.pose, 1, 1, 1).uv(sideActivity.getU0, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 1, 1, 0).uv(sideActivity.getU1, sideActivity.getV1).endVertex()
        r.vertex(stack.last.pose, 1, 0, 0).uv(sideActivity.getU1, sideActivity.getV0).endVertex()
        r.vertex(stack.last.pose, 1, 0, 1).uv(sideActivity.getU0, sideActivity.getV0).endVertex()
      }

      stack.popPose()
    }

    RenderState.checkError(getClass.getName + ".render: leaving")
  }
}
