package li.cil.oc.api.prefab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.oc.api.manual.TabIconRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;

/**
 * Simple implementation of a tab icon renderer using an item stack as its graphic.
 */
@SuppressWarnings("UnusedDeclaration")
public class ItemStackTabIconRenderer implements TabIconRenderer {
    private final ItemStack stack;

    public ItemStackTabIconRenderer(ItemStack stack) {
        this.stack = stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @ApiStatus.Experimental
    public void render(PoseStack matrix) {
        // Translate manually because ItemRenderer generally can't take a MatrixStack.

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(matrix.last().pose());
        RenderSystem.setShaderTexture(240, 240);
        //RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, 240, 240);
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, 0, 0);
        modelViewStack.popPose();
    }
}
