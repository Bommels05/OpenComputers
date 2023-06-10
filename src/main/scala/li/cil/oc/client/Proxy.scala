package li.cil.oc.client

import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.OpenComputers
import li.cil.oc.api
import li.cil.oc.client
import li.cil.oc.client.gui.GuiTypes
import li.cil.oc.client.renderer.HighlightRenderer
import li.cil.oc.client.renderer.MFUTargetRenderer
import li.cil.oc.client.renderer.PetRenderer
import li.cil.oc.client.renderer.TextBufferRenderCache
import li.cil.oc.client.renderer.WirelessNetworkDebugRenderer
import li.cil.oc.client.renderer.block.ModelInitialization
import li.cil.oc.client.renderer.block.NetSplitterModel
import li.cil.oc.client.renderer.entity.DroneRenderer
import li.cil.oc.client.renderer.blockentity._
import li.cil.oc.common
import li.cil.oc.common.{PacketHandler => CommonPacketHandler}
import li.cil.oc.common.{Proxy => CommonProxy}
import li.cil.oc.common.component.TextBuffer
import li.cil.oc.common.entity.Drone
import li.cil.oc.common.entity.EntityTypes
import li.cil.oc.common.event.NanomachinesHandler
import li.cil.oc.common.event.RackMountableRenderHandler
import li.cil.oc.common.blockentity
import li.cil.oc.common.blockentity.Adapter
import li.cil.oc.util.Audio
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer, BlockEntityRendererProvider, BlockEntityRenderers}
import net.minecraft.client.renderer.entity.{EntityRenderer, EntityRendererProvider}
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.client.ClientRegistry
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.ForgeRegistries

private[oc] class Proxy extends CommonProxy {
  modBus.register(classOf[GuiTypes])
  modBus.register(ModelInitialization)
  modBus.register(NetSplitterModel)
  modBus.register(Textures)

  override def preInit() {
    super.preInit()

    api.API.manual = client.Manual
  }

  override def init(e: FMLCommonSetupEvent) {
    super.init(e)

    CommonPacketHandler.clientHandler = PacketHandler

    e.enqueueWork((() => {
      ModelInitialization.preInit()

      ColorHandler.init()

      BlockEntityRenderers.register(blockentity.BlockEntityTypes.ADAPTER, (context: BlockEntityRendererProvider.Context) => new AdapterRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.ASSEMBLER, (context: BlockEntityRendererProvider.Context) => new AssemblerRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.CASE, (context: BlockEntityRendererProvider.Context) => new CaseRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.CHARGER, (context: BlockEntityRendererProvider.Context) => new ChargerRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.DISASSEMBLER, (context: BlockEntityRendererProvider.Context) => new DisassemblerRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.DISK_DRIVE, (context: BlockEntityRendererProvider.Context) => new DiskDriveRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.GEOLYZER, (context: BlockEntityRendererProvider.Context) => new GeolyzerRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.HOLOGRAM, (context: BlockEntityRendererProvider.Context) => new HologramRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.MICROCONTROLLER, (context: BlockEntityRendererProvider.Context) => new MicrocontrollerRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.NET_SPLITTER, (context: BlockEntityRendererProvider.Context) => new NetSplitterRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.POWER_DISTRIBUTOR, (context: BlockEntityRendererProvider.Context) => new PowerDistributorRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.PRINTER, (context: BlockEntityRendererProvider.Context) => new PrinterRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.RAID, (context: BlockEntityRendererProvider.Context) => new RaidRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.RACK, (context: BlockEntityRendererProvider.Context) => new RackRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.RELAY, (context: BlockEntityRendererProvider.Context) => new RelayRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.ROBOT, (context: BlockEntityRendererProvider.Context) => new RobotRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.SCREEN, (context: BlockEntityRendererProvider.Context) => new ScreenRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])
      BlockEntityRenderers.register(blockentity.BlockEntityTypes.TRANSPOSER, (context: BlockEntityRendererProvider.Context) => new TransposerRenderer(context.getBlockEntityRenderDispatcher).asInstanceOf[BlockEntityRenderer[BlockEntity]])

      ClientRegistry.registerKeyBinding(KeyBindings.extendedTooltip)
      ClientRegistry.registerKeyBinding(KeyBindings.analyzeCopyAddr)
      ClientRegistry.registerKeyBinding(KeyBindings.clipboardPaste)

      MinecraftForge.EVENT_BUS.register(HighlightRenderer)
      MinecraftForge.EVENT_BUS.register(NanomachinesHandler.Client)
      MinecraftForge.EVENT_BUS.register(PetRenderer)
      MinecraftForge.EVENT_BUS.register(RackMountableRenderHandler)
      MinecraftForge.EVENT_BUS.register(Sound)
      MinecraftForge.EVENT_BUS.register(TextBuffer)
      MinecraftForge.EVENT_BUS.register(MFUTargetRenderer)
      MinecraftForge.EVENT_BUS.register(WirelessNetworkDebugRenderer)
      MinecraftForge.EVENT_BUS.register(Audio)
      MinecraftForge.EVENT_BUS.register(HologramRenderer)
      MinecraftForge.EVENT_BUS.register(this)
    }): Runnable)

    RenderSystem.recordRenderCall(() => MinecraftForge.EVENT_BUS.register(TextBufferRenderCache))
  }

  override def registerModel(instance: Item, id: String): Unit = ModelInitialization.registerModel(instance, id)

  override def registerModel(instance: Block, id: String): Unit = ModelInitialization.registerModel(instance, id)

  @SubscribeEvent
  def onRegisterEntityRenderers(e: EntityRenderersEvent.RegisterRenderers): Unit = {
    e.registerEntityRenderer(EntityTypes.DRONE, (context: EntityRendererProvider.Context) => new DroneRenderer(context))
  }
}
