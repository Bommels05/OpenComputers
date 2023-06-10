package li.cil.oc.common.block

import java.util
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.client.KeyBindings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.item.data.RobotData
import li.cil.oc.common.blockentity
import li.cil.oc.server.PacketSender
import li.cil.oc.server.agent
import li.cil.oc.server.loot.LootFunctions
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.InventoryUtils
import li.cil.oc.util.Tooltip
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}

import scala.collection.convert.ImplicitConversionsToScala._

class RobotProxy(props: Properties) extends RedstoneAware(props, blockentity.BlockEntityTypes.ROBOT) with traits.StateAware {
  val shape = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9)

  override val getDescriptionId = "robot"

  var moving = new ThreadLocal[Option[blockentity.Robot]] {
    override protected def initialValue = None
  }

  // ----------------------------------------------------------------------- //

  override def getCloneItemStack(state: BlockState, target: HitResult, world: BlockGetter, pos: BlockPos, player: Player): ItemStack =
    world.getBlockEntity(pos) match {
      case proxy: blockentity.RobotProxy => proxy.robot.info.copyItemStack()
      case _ => ItemStack.EMPTY
    }

  override def getShape(state: BlockState, world: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape = {
    world.getBlockEntity(pos) match {
      case proxy: blockentity.RobotProxy =>
        val robot = proxy.robot
        if (robot.isAnimatingMove) {
          val remaining = robot.animationTicksLeft.toDouble / robot.animationTicksTotal.toDouble
          val blockPos = robot.moveFrom.get
          val vec = robot.getBlockPos
          val delta = new BlockPos(blockPos.getX - vec.getX, blockPos.getY - vec.getY, blockPos.getZ - vec.getZ)
          shape.move(delta.getX * remaining, delta.getY * remaining, delta.getZ * remaining)
        }
        else shape
      case _ => super.getShape(state, world, pos, ctx)
    }
  }

  // ----------------------------------------------------------------------- //

  override protected def tooltipHead(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: TooltipFlag) {
    super.tooltipHead(stack, world, tooltip, advanced)
    addLines(stack, tooltip)
  }

  override protected def tooltipBody(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: TooltipFlag) {
    for (curr <- Tooltip.get("robot")) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  override protected def tooltipTail(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], flag: TooltipFlag) {
    super.tooltipTail(stack, world, tooltip, flag)
    if (KeyBindings.showExtendedTooltips) {
      val info = new RobotData(stack)
      val components = info.containers ++ info.components
      if (components.length > 0) {
        for (curr <- Tooltip.get("server.Components")) {
          tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
        }
        for (component <- components if !component.isEmpty) {
          tooltip.add(new TextComponent("- " + component.getHoverName.getString).setStyle(Tooltip.DefaultStyle))
        }
      }
    }
  }

  private def addLines(stack: ItemStack, tooltip: util.List[Component]) {
    if (stack.hasTag) {
      if (stack.getTag.contains(Settings.namespace + "xp")) {
        val xp = stack.getTag.getDouble(Settings.namespace + "xp")
        val level = Math.min((Math.pow(xp - Settings.get.baseXpToLevel, 1 / Settings.get.exponentialXpGrowth) / Settings.get.constantXpGrowth).toInt, 30)
        if (level > 0) {
          for (curr <- Tooltip.get(getDescriptionId + "_level", level)) {
            tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
          }
        }
      }
      if (stack.getTag.contains(Settings.namespace + "storedEnergy")) {
        val energy = stack.getTag.getInt(Settings.namespace + "storedEnergy")
        if (energy > 0) {
          for (curr <- Tooltip.get(getDescriptionId + "_storedenergy", energy)) {
            tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
          }
        }
      }
    }
  }

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState): blockentity.RobotProxy = {
    moving.get match {
      case Some(robot) => new blockentity.RobotProxy(blockentity.BlockEntityTypes.ROBOT, robot, pos, state)
      case _ => new blockentity.RobotProxy(blockentity.BlockEntityTypes.ROBOT, pos, state)
    }
  }

  // ----------------------------------------------------------------------- //

  override def getDrops(state: BlockState, ctx: LootContext.Builder): util.List[ItemStack] = {
    // Superspecial hack... usually this will not work, because Minecraft calls
    // this method *after* the block has already been destroyed. Meaning we
    // won't have access to the tile entity.
    // However! Some mods with block breakers, specifically AE2's annihilation
    // plane, will call *only* this method (don't use a fake player to call
    // removedByPlayer), but call it *before* the block was destroyed. So in
    // general it *should* be safe to generate the item here if the tile entity
    // still exists, and always spawn the stack in removedByPlayer... if some
    // mod calls this before the block is broken *and* calls removedByPlayer
    // this will lead to dupes, but in some initial testing this wasn't the
    // case anywhere (TE autonomous activator, CC turtles).
    val newCtx = ctx.withDynamicDrop(LootFunctions.DYN_ITEM_DATA, (c, f) => {
      c.getParamOrNull(LootContextParams.BLOCK_ENTITY) match {
        case proxy: blockentity.RobotProxy =>
          val robot = proxy.robot
          if (robot.node != null) {
            // Update: even more special hack! As discussed here http://git.io/IcNAyg
            // some mods call this even when they're not about to actually break the
            // block... soooo we need a whitelist to know when to generate a *proper*
            // drop (i.e. with file systems closed / open handles not saved, e.g.).
            if (gettingDropsForActualDrop) {
              robot.node.remove()
              robot.saveComponents()
            }
            f.accept(robot.info.createItemStack())
          }
        case _ =>
      }
    })

    super.getDrops(state, newCtx)
  }

  private val getDropForRealDropCallers = Set(
    "appeng.parts.automation.PartAnnihilationPlane.EatBlock"
  )

  private def gettingDropsForActualDrop = new Exception().getStackTrace.exists(element => getDropForRealDropCallers.contains(element.getClassName + "." + element.getMethodName))

  // ----------------------------------------------------------------------- //

  override def localOnBlockActivated(world: Level, pos: BlockPos, player: Player, hand: InteractionHand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!player.isCrouching) {
      if (!world.isClientSide) {
        // We only send slot changes to nearby players, so if there was no slot
        // change since this player got into range he might have the wrong one,
        // so we send him the current one just in case.
        (player, world.getBlockEntity(pos)) match {
          case (srvPlr: ServerPlayer, proxy: blockentity.RobotProxy) if proxy.robot.node.network != null =>
            PacketSender.sendRobotSelectedSlotChange(proxy.robot)
            if (proxy.stillValid(player)) {
              ContainerTypes.openRobotGui(srvPlr, proxy.robot)
            }
          case _ =>
        }
      }
      true
    }
    else if (heldItem.isEmpty) {
      if (!world.isClientSide) {
        world.getBlockEntity(pos) match {
          case proxy: blockentity.RobotProxy if !proxy.machine.isRunning && proxy.stillValid(player) => proxy.machine.start()
          case _ =>
        }
      }
      true
    }
    else false
  }

  override def setPlacedBy(world: Level, pos: BlockPos, state: BlockState, entity: LivingEntity, stack: ItemStack) {
    super.setPlacedBy(world, pos, state, entity, stack)
    if (!world.isClientSide) ((entity, world.getBlockEntity(pos)) match {
      case (player: agent.Player, proxy: blockentity.RobotProxy) =>
        Some((proxy.robot, player.agent.ownerName, player.agent.ownerUUID))
      case (player: Player, proxy: blockentity.RobotProxy) =>
        Some((proxy.robot, player.getName.getString, player.getGameProfile.getId))
      case _ => None
    }) match {
      case Some((robot, owner, uuid)) =>
        robot.ownerName = owner
        robot.ownerUUID = agent.Player.determineUUID(Option(uuid))
        robot.info.loadData(stack)
        robot.bot.node.changeBuffer(robot.info.robotEnergy - robot.bot.node.localBuffer)
        robot.updateInventorySize()
      case _ =>
    }
  }

  override def onDestroyedByPlayer(state: BlockState, world: Level, pos: BlockPos, player: Player, willHarvest: Boolean, fluid: FluidState): Boolean = {
    world.getBlockEntity(pos) match {
      case proxy: blockentity.RobotProxy =>
        val robot = proxy.robot
        // Only allow breaking creative tier robots by allowed users.
        // Unlike normal robots, griefing isn't really a valid concern
        // here, because to get a creative robot you need creative
        // mode in the first place.
        if (robot.isCreative && (!player.isCreative || !robot.canInteract(player.getName.getString))) return false
        if (!world.isClientSide) {
          if (robot.player == player) return false
          robot.node.remove()
          robot.saveComponents()
          if (player.isCreative) InventoryUtils.spawnStackInWorld(BlockPosition(pos, world), robot.info.createItemStack())
        }
        robot.moveFrom.foreach(fromPos => if (world.getBlockState(fromPos).getBlock == api.Items.get(Constants.BlockName.RobotAfterimage).block) {
          world.setBlock(fromPos, Blocks.AIR.defaultBlockState, 1)
        })
      case _ =>
    }
    super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid)
  }
}
