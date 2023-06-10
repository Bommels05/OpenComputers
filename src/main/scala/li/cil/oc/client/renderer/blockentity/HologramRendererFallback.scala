package li.cil.oc.client.renderer.blockentity

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import li.cil.oc.common.blockentity.Hologram
import li.cil.oc.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{GameRenderer, MultiBufferSource}

object HologramRendererFallback {
  var text = "Requires OpenGL 1.5"

  def render(hologram: Hologram, f: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    val fontRenderer = Minecraft.getInstance.font

    stack.pushPose()
    stack.translate(0.5, 0.75, 0.5)
    stack.scale(1 / 128f, -1 / 128f, 1 / 128f)

    fontRenderer.drawInBatch(text, -fontRenderer.width(text) / 2, 0, 0xFFFFFFFF,
      false, stack.last.pose, buffer, false, 0, light)
    stack.mulPose(Vector3f.YP.rotationDegrees(180))
    fontRenderer.drawInBatch(text, -fontRenderer.width(text) / 2, 0, 0xFFFFFFFF,
      false, stack.last.pose, buffer, false, 0, light)

    stack.popPose()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }
}
