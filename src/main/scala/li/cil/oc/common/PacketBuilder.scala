package li.cil.oc.common

import java.util.function.Supplier
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import io.netty.buffer.Unpooled
import li.cil.oc.OpenComputers
import li.cil.oc.api.network.EnvironmentHost
import net.minecraft.core.Direction
import net.minecraft.nbt.{CompoundTag, NbtIo}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.registries._
import net.minecraftforge.server.ServerLifecycleHooks

import scala.collection.convert.ImplicitConversionsToScala._

abstract class PacketBuilder(stream: OutputStream) extends DataOutputStream(stream) {
  def writeRegistryEntry[T <: IForgeRegistryEntry[T]](registry: IForgeRegistry[T], value: T): Unit =
    writeInt(registry.asInstanceOf[ForgeRegistry[T]].getID(value))

  def writeBlockEntity(b: BlockEntity) {
    writeUTF(b.getLevel.dimension.location.toString)
    writeInt(b.getBlockPos.getX)
    writeInt(b.getBlockPos.getY)
    writeInt(b.getBlockPos.getZ)
  }

  def writeEntity(e: Entity) {
    writeUTF(e.level.dimension.location.toString)
    writeInt(e.getId)
  }

  def writeDirection(d: Option[Direction]) = d match {
    case Some(side) => writeByte(side.ordinal.toByte)
    case _ => writeByte(-1: Byte)
  }

  def writeItemStack(stack: ItemStack) = {
    val haveStack = !stack.isEmpty && stack.getCount > 0
    writeBoolean(haveStack)
    if (haveStack) {
      writeNBT(stack.save(new CompoundTag()))
    }
  }

  def writeNBT(nbt: CompoundTag) = {
    val haveNbt = nbt != null
    writeBoolean(haveNbt)
    if (haveNbt) {
      NbtIo.write(nbt, this)
    }
  }

  def writeMedium(v: Int) = {
    writeByte(v & 0xFF)
    writeByte((v >> 8) & 0xFF)
    writeByte((v >> 16) & 0xFF)
  }

  def writePacketType(pt: PacketType.Value) = writeByte(pt.id)

  def sendToAllPlayers() = OpenComputers.channel.send(PacketDistributor.ALL.noArg(), packet)

  def sendToPlayersNearEntity(e: Entity, range: Option[Double] = None): Unit = sendToNearbyPlayers(e.level, e.getX, e.getY, e.getZ, range)

  def sendToPlayersNearBlockEntity(b: BlockEntity, range: Option[Double] = None): Unit = sendToNearbyPlayers(b.getLevel, b.getBlockPos.getX + 0.5, b.getBlockPos.getY + 0.5, b.getBlockPos.getZ + 0.5, range)

  def sendToPlayersNearHost(host: EnvironmentHost, range: Option[Double] = None): Unit = sendToNearbyPlayers(host.world, host.xPosition, host.yPosition, host.zPosition, range)

  def sendToNearbyPlayers(world: Level, x: Double, y: Double, z: Double, range: Option[Double]) {
    val server = ServerLifecycleHooks.getCurrentServer
    val manager = server.getPlayerList
    for (player <- manager.getPlayers if player.level == world) {
      val playerRenderDistance = 16
      val playerSpecificRange = range.getOrElse((manager.getViewDistance min playerRenderDistance) * 16.0)
      if (player.distanceToSqr(x, y, z) < playerSpecificRange * playerSpecificRange) {
        sendToPlayer(player)
      }
    }
  }

  def sendToPlayer(player: ServerPlayer) = OpenComputers.channel.send(PacketDistributor.PLAYER.`with`(new Supplier[ServerPlayer] {
    override def get = player
  }), packet)

  def sendToServer() = OpenComputers.channel.sendToServer(packet)

  protected def packet: Array[Byte]
}

// Necessary to keep track of the GZIP stream.
abstract class PacketBuilderBase[T <: OutputStream](protected val stream: T) extends PacketBuilder(new BufferedOutputStream(stream))

class SimplePacketBuilder(val packetType: PacketType.Value) extends PacketBuilderBase(PacketBuilder.newData(compressed = false)) {
  writeByte(packetType.id)

  override protected def packet = {
    flush()
    stream.toByteArray
  }
}

class CompressedPacketBuilder(val packetType: PacketType.Value, private val data: ByteArrayOutputStream = PacketBuilder.newData(compressed = true)) extends PacketBuilderBase(new DeflaterOutputStream(data, new Deflater(Deflater.BEST_SPEED))) {
  writeByte(packetType.id)

  override protected def packet = {
    flush()
    stream.finish()
    data.toByteArray
  }
}

object PacketBuilder {
  def newData(compressed: Boolean) = {
    val data = new ByteArrayOutputStream
    data.write(if (compressed) 1 else 0)
    data
  }
}
