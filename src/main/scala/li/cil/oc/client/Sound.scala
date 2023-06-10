package li.cil.oc.client

import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import com.google.common.base.Charsets
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.{AbstractSoundInstance, TickableSoundInstance}
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.world.WorldEvent

import scala.collection.mutable

object Sound {
  private val sources = mutable.Map.empty[BlockEntity, PseudoLoopingStream]

  private val commandQueue = mutable.PriorityQueue.empty[Command]

  private val updateTimer = new Timer("OpenComputers-SoundUpdater", true)
  if (Settings.get.soundVolume > 0) {
    updateTimer.scheduleAtFixedRate(new TimerTask {
      override def run() {
        sources.synchronized(Sound.updateCallable = Some(() => processQueue()))
      }
    }, 500, 50)
  }

  private var updateCallable = None: Option[() => Unit]

  private def processQueue() {
    if (commandQueue.nonEmpty) {
      commandQueue.synchronized {
        while (commandQueue.nonEmpty && commandQueue.head.when < System.currentTimeMillis()) {
          try commandQueue.dequeue()() catch {
            case t: Throwable => OpenComputers.log.warn("Error processing sound command.", t)
          }
        }
      }
    }
  }

  def startLoop(blockEntity: BlockEntity, name: String, volume: Float = 1f, delay: Long = 0) {
    if (Settings.get.soundVolume > 0) {
      commandQueue.synchronized {
        commandQueue += new StartCommand(System.currentTimeMillis() + delay, blockEntity, name, volume)
      }
    }
  }

  def stopLoop(blockEntity: BlockEntity) {
    if (Settings.get.soundVolume > 0) {
      commandQueue.synchronized {
        commandQueue += new StopCommand(blockEntity)
      }
    }
  }

  def updatePosition(blockEntity: BlockEntity) {
    if (Settings.get.soundVolume > 0) {
      commandQueue.synchronized {
        commandQueue += new UpdatePositionCommand(blockEntity)
      }
    }
  }

  @SubscribeEvent
  def onTick(e: ClientTickEvent) {
    sources.synchronized {
      updateCallable.foreach(_ ())
      updateCallable = None
    }
  }

  @SubscribeEvent
  def onWorldUnload(event: WorldEvent.Unload) {
    commandQueue.synchronized(commandQueue.clear())
    sources.synchronized(try sources.foreach(_._2.stop()) catch {
      case _: Throwable => // Ignore.
    })
    sources.clear()
  }

  private abstract class Command(val when: Long, val blockEntity: BlockEntity) extends Ordered[Command] {
    def apply(): Unit

    override def compare(that: Command) = (that.when - when).toInt
  }

  private class StartCommand(when: Long, blockEntity: BlockEntity, val name: String, val volume: Float) extends Command(when, blockEntity) {
    override def apply() {
      sources.synchronized {
        val current = sources.getOrElse(blockEntity, null)
        if (current == null || !current.getLocation.getPath.equals(name)) {
          if (current != null) current.stop()
          sources(blockEntity) = new PseudoLoopingStream(blockEntity, volume, name)
        }
      }
    }
  }

  private class StopCommand(blockEntity: BlockEntity) extends Command(System.currentTimeMillis() + 1, blockEntity) {
    override def apply() {
      sources.synchronized {
        sources.remove(blockEntity) match {
          case Some(sound) => sound.stop()
          case _ =>
        }
      }
      commandQueue.synchronized {
        // Remove all other commands for this tile entity from the queue. This
        // is inefficient, but we generally don't expect the command queue to
        // be very long, so this should be OK.
        commandQueue ++= commandQueue.dequeueAll.filter(_.blockEntity != blockEntity)
      }
    }
  }

  private class UpdatePositionCommand(blockEntity: BlockEntity) extends Command(System.currentTimeMillis(), blockEntity) {
    override def apply() {
      sources.synchronized {
        sources.get(blockEntity) match {
          case Some(sound) => sound.updatePosition()
          case _ =>
        }
      }
    }
  }

  private class PseudoLoopingStream(val tileEntity: BlockEntity, val subVolume: Float, name: String)
    extends AbstractSoundInstance(new ResourceLocation(OpenComputers.ID, name), SoundSource.BLOCKS) with TickableSoundInstance {

    var stopped = false
    volume = subVolume * Settings.get.soundVolume
    relative = tileEntity != null
    looping = true
    updatePosition()

    def updatePosition() {
      if (tileEntity != null) {
        val pos = tileEntity.getBlockPos
        x = pos.getX + 0.5
        y = pos.getY + 0.5
        z = pos.getZ + 0.5
      }
    }

    override def canStartSilent() = true

    override def isStopped() = stopped

    // Required by TickableSoundInstance, which is required to update position while playing
    override def tick() = ()

    def stop() {
      stopped = true
      looping = false
    }
  }
}
