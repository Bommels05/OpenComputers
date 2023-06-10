package li.cil.oc.client.renderer.markdown.segment.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import li.cil.oc.api.manual.ImageRenderer
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL13

private[markdown] class ItemStackImageRenderer(val stacks: Array[ItemStack]) extends ImageRenderer {
  // How long to show individual stacks, in milliseconds, before switching to the next.
  final val cycleSpeed = 1000

  override def getWidth = 32

  override def getHeight = 32

  override def render(matrix: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    val mc = Minecraft.getInstance
    val index = (System.currentTimeMillis() % (cycleSpeed * stacks.length)).toInt / cycleSpeed
    val stack = stacks(index)

    matrix.scale(getWidth / 16, getHeight / 16, getWidth / 16)
    // Translate manually because ItemRenderer generally can't take a MatrixStack.
    val renderStack = RenderSystem.getModelViewStack;
    renderStack.pushPose()
    renderStack.mulPoseMatrix(matrix.last().pose())
    GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, 240, 240)
    mc.getItemRenderer.renderAndDecorateItem(stack, 0, 0)
    renderStack.popPose()
  }
}
