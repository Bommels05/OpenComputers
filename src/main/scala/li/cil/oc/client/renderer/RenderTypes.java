package li.cil.oc.client.renderer;

import java.util.OptionalDouble;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import li.cil.oc.OpenComputers;
import li.cil.oc.client.Textures;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderTypes extends RenderType {
    public static final VertexFormat POSITION_TEX_NORMAL = new VertexFormat(new ImmutableMap.Builder<String, VertexFormatElement>()
        .put("position", DefaultVertexFormat.ELEMENT_POSITION)
        .put("uv0", DefaultVertexFormat.ELEMENT_UV0)
        .put("normal", DefaultVertexFormat.ELEMENT_NORMAL)
        .put("padding", DefaultVertexFormat.ELEMENT_PADDING)
        .build());

    public static final TextureStateShard ROBOT_CHASSIS_TEXTURE = new TextureStateShard(Textures.Model$.MODULE$.Robot(), false, false);

    public static final RenderType ROBOT_CHASSIS = create(OpenComputers.ID() + ":robot_chassis",
        DefaultVertexFormat.BLOCK, VertexFormat.Mode.TRIANGLES, 1024, CompositeState.builder()
            .setTextureState(ROBOT_CHASSIS_TEXTURE)
            //.setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setLightmapState(LIGHTMAP)
            .createCompositeState(true));

    public static final RenderType ROBOT_LIGHT = create(OpenComputers.ID() + ":robot_light",
        DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, CompositeState.builder()
            .setTextureState(ROBOT_CHASSIS_TEXTURE)
            .setTransparencyState(LIGHTNING_TRANSPARENCY)
            .createCompositeState(true));

    private static final RenderType createUpgrade(String name, ResourceLocation texture) {
        return create(OpenComputers.ID() + ":upgrade_" + name,
            POSITION_TEX_NORMAL, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                .setTextureState(new TextureStateShard(texture, false, false))
                .createCompositeState(true));
    }

    public static final RenderType UPGRADE_CRAFTING = createUpgrade("crafting", Textures.Model$.MODULE$.UpgradeCrafting());

    public static final RenderType UPGRADE_GENERATOR = createUpgrade("generator", Textures.Model$.MODULE$.UpgradeGenerator());

    public static final RenderType UPGRADE_INVENTORY = createUpgrade("inventory", Textures.Model$.MODULE$.UpgradeInventory());

    public static final RenderType MFU_LINES = create(OpenComputers.ID() + ":mfu_lines",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 1024, CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(NO_DEPTH_TEST)
                .setOutputState(TRANSLUCENT_TARGET)
                .setLineState(new LineStateShard(OptionalDouble.of(2.0)))
                .createCompositeState(false));

    public static final RenderType MFU_QUADS = create(OpenComputers.ID() + ":mfu_quads",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(NO_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setOutputState(TRANSLUCENT_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));

    public static final RenderType BLOCK_OVERLAY = create(OpenComputers.ID() + ":overlay_block",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                //.setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false));

    public static final RenderType BLOCK_OVERLAY_COLOR = create(OpenComputers.ID() + ":overlay_block",
            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                //.setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false));

    public static final RenderType FONT_QUAD = create(OpenComputers.ID() + ":font_quad",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));

    private static class CustomTextureState extends TexturingStateShard {
        public CustomTextureState(int id) {
            super("custom_tex_" + id, () -> {
                // Should already be enabled, but vanilla does it too.
                RenderSystem.enableTexture();
                RenderSystem.bindTexture(id);
            }, () -> {});
        }
    }

    private static class LinearTexturingState extends TexturingStateShard {
        public LinearTexturingState(boolean linear) {
            super(linear ? "lin_font_texturing" : "near_font_texturing", () -> {
                // Texture is already bound, only have to make set minify filter.
                if (linear) GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                else GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            }, () -> {
                // Nothing to do, the texture was already unbound.
            });
        }
    }

    private static final LinearTexturingState NEAR = new LinearTexturingState(false);
    private static final LinearTexturingState LINEAR = new LinearTexturingState(true);

    public static final RenderType createFontTex(String name, ResourceLocation texture, boolean linear) {
        return create(OpenComputers.ID() + ":font_stat_" + name,
            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                // First parameter is blur (i.e. linear filter).
                // We can't use it because it's also MAG_FILTER.
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                //.setAlphaState(DEFAULT_ALPHA)
                .setTexturingState(linear ? LINEAR : NEAR)
                .createCompositeState(false));
    }

    public static final RenderType createFontTex(int id) {
        return create(OpenComputers.ID() + ":font_dyn_" + id,
            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                .setTexturingState(new CustomTextureState(id))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                //.setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false));
    }

    public static final RenderType createTexturedQuad(String name, ResourceLocation texture, VertexFormat format, boolean additive) {
        return create(OpenComputers.ID() + ":tex_quad_" + name,
            format, VertexFormat.Mode.QUADS, 1024, CompositeState.builder()
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(additive ? LIGHTNING_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                //.setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false));
    }

    private RenderTypes() {
        super(null, null, null, 0, false, false, null, null);
        throw new Error();
    }
}
