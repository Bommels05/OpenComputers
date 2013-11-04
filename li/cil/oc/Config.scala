package li.cil.oc

import java.io.File

object Config {
  val resourceDomain = "opencomputers"
  val savePath = "opencomputers/"
  val scriptPath = "/assets/" + resourceDomain + "/lua/"

  // ----------------------------------------------------------------------- //

  val screenResolutionsByTier = Array((50, 16), (80, 25), (160, 50))

  // ----------------------------------------------------------------------- //

  // Power it takes to run a computer at 100% CPU time for one second.
  val cpuTimeCost = 256

  // Power it takes to change a single pixel via the set command.
  val screenSetCost = 1.0 / 20

  // Power it takes to change a single pixel via the fill command.
  val screenFillCost = 1.0 / 180

  // Power it takes to change a single pixel to blank via the fill command.
  val screenClearCost = 1.0 / 200

  // Power it takes to move a single pixel via the copy command.
  val screenCopyCost = 1.0 / 160

  // ----------------------------------------------------------------------- //

  var blockRenderId = 0

  // ----------------------------------------------------------------------- //

  var blockId = 3650
  var blockSpecialId = 3651
  var itemId = 4600

  // ----------------------------------------------------------------------- //

  var maxScreenTextRenderDistance = 10.0
  var screenTextFadeStartDistance = 8.0

  // ----------------------------------------------------------------------- //

  var baseMemory = 0
  var commandUser = "OpenComputers"
  var fileCost = 512
  var filesBuffered = true
  var ignorePower = false
  var maxHandles = 16
  var maxReadBuffer = 8 * 1024
  var maxScreenHeight = 6
  var maxScreenWidth = 8
  var threads = 4
  var timeout = 3.0

  // ----------------------------------------------------------------------- //

  def load(file: File) = {
    val config = new net.minecraftforge.common.Configuration(file)

    // --------------------------------------------------------------------- //

    blockId = config.getBlock("block", blockId,
      "The block ID used for simple blocks.").
      getInt(blockId)

    blockSpecialId = config.getBlock("blockSpecial", blockSpecialId,
      "The block ID used for special blocks.").
      getInt(blockSpecialId)

    itemId = config.getItem("item", itemId,
      "The item ID used for all non-stackable items.").
      getInt(itemId)

    // --------------------------------------------------------------------- //

    config.getCategory("client").
      setComment("Client side settings, presentation and performance related stuff.")

    maxScreenTextRenderDistance = config.get("client", "maxScreenTextRenderDistance", maxScreenTextRenderDistance, "" +
      "The maximum distance at which to render text on screens. Rendering text\n" +
      "can be pretty expensive, so if you have a lot of screens you'll want\n" +
      "to avoid huge numbers here. Note that this setting is client-sided, and\n" +
      "only has an impact on render performance on clients.").
      getDouble(maxScreenTextRenderDistance)

    screenTextFadeStartDistance = config.get("client", "screenTextFadeStartDistance", screenTextFadeStartDistance, "" +
      "The distance at which to start fading out the text on screens. This is\n" +
      "purely cosmetic, to avoid text disappearing instantly when moving too far\n" +
      "away from a screen. This should have no measurable impact on performance.\n" +
      "Note that this needs OpenGL 1.4 to work, otherwise text will always just\n" +
      "instantly disappear when moving away from the screen displaying it.").
      getDouble(screenTextFadeStartDistance)

    // --------------------------------------------------------------------- //

    config.getCategory("server").
      setComment("Server side settings, gameplay and security related stuff.")

    baseMemory = config.get("server", "baseMemory", baseMemory, "" +
      "The base amount of memory made available in computers even if they have no\n" +
      "RAM installed. Use this if you feel you can't get enough RAM using the\n" +
      "given means, that being RAM components. Just keep in mind that this is\n" +
      "global and applies to all computers!").
      getInt(baseMemory)

    commandUser = config.get("server", "commandUser", commandUser, "" +
      "The user name to specify when executing a command via a command block. If\n" +
      "you leave this empty it will use the address of the network node that sent\n" +
      "the execution request - which will usually be a computer.").
      getString.trim

    fileCost = config.get("server", "fileCost", fileCost, "" +
      "The base 'cost' of a single file or directory on a limited file system,\n" +
      "such as hard drives. When computing the used space we add this cost to\n" +
      "the real size of each file (and folders, which are zero sized otherwise).\n" +
      "This is to ensure that users cannot spam the file system with an infinite\n" +
      "number of files and/or folders. Note that the size returned via fs.size\n" +
      "will always be the real file size, however.").
      getInt(fileCost) max 0

    filesBuffered = config.get("server", "filesBuffered", filesBuffered, "" +
      "Whether persistent file systems such as disk drivers should be 'buffered',\n" +
      "and only written to disk when the world is saved. This applies to all hard\n" +
      "drives. The advantage of having this enabled is that data will never go\n" +
      "'out of sync' with the computer's state if the game crashes. The price is\n" +
      "slightly higher memory consumption, since all loaded files have to be kept\n" +
      "in memory (loaded as in when the hard drive is in a computer).").
      getBoolean(filesBuffered)

    ignorePower = config.get("server", "ignorePower", ignorePower, "" +
      "Whether to ignore any power requirements. Whenever something requires\n" +
      "power to function, it will try to get the amount of power it needs from\n" +
      "the buffer of its connector node, and in case it fails it won't perform" +
      "the action / trigger a shutdown / whatever. Setting this to `true` will\n" +
      "simply make the check 'is there enough power' succeed unconditionally.\n" +
      "Note that buffers are still filled and emptied following the usual rules,\n" +
      "there just is no failure case anymore.").
      getBoolean(ignorePower)

    maxHandles = config.get("server", "maxHandles", maxHandles, "" +
      "The maximum number of file handles any single computer may have open at a\n" +
      "time. Note that this is *per filesystem*. Also note that this is only\n" +
      "enforced by the filesystem node - if an addon decides to be fancy it may\n" +
      "well ignore this. Since file systems are usually 'virtual' this will\n" +
      "usually not have any real impact on performance/not be noticeable on the\n" +
      "host operating system.")
      .getInt(maxHandles) max 1

    maxReadBuffer = config.get("server", "maxReadBuffer", maxReadBuffer, "" +
      "The maximum block size that can be read in one 'read' call on a file\n" +
      "system. This is used to limit the amount of memory a call from a user\n" +
      "program can cause to be allocated on the host side: when 'read' is called,\n" +
      "a byte array with the specified size has to be allocated. So if this\n" +
      "weren't limited, a Lua program could trigger massive memory allocations\n" +
      "regardless of the amount of RAM installed in the computer it runs on. As a\n" +
      "side effect this pretty much determines the read performance of file\n" +
      "systems.")
      .getInt(maxReadBuffer) max 1

    maxScreenHeight = config.get("server", "maxScreenHeight", maxScreenHeight, "" +
      "The maximum height of multi-block screens, in blocks. This is limited to\n" +
      "avoid excessive computations for merging screens. If you really need\n" +
      "bigger screens it's probably safe to bump this quite a bit before you\n" +
      "notice anything, since at least incremental updates should be very\n" +
      "efficient (i.e. when adding/removing a single screen).")
      .getInt(maxScreenHeight) max 1

    maxScreenWidth = config.get("server", "maxScreenWidth", maxScreenWidth, "" +
      "The maximum width of multi-block screens, in blocks. See maxScreenHeight.")
      .getInt(maxScreenWidth) max 1

    threads = config.get("server", "threads", threads, "" +
      "The overall number of threads to use to drive computers. Whenever a\n" +
      "computer should run, for example because a signal should be processed or\n" +
      "some sleep timer expired it is queued for execution by a worker thread.\n" +
      "The higher the number of worker threads, the less likely it will be that\n" +
      "computers block each other from running, but the higher the host system's\n" +
      "load may become.").
      getInt(threads) max 1

    timeout = config.get("server", "timeout", timeout, "" +
      "The time in seconds a program may run without yielding before it is\n" +
      "forcibly aborted. This is used to avoid stupidly written or malicious\n" +
      "programs blocking other computers by locking down the executor threads.\n" +
      "Note that changing this won't have any effect on computers that are\n" +
      "already running - they'll have to be rebooted for this to take effect.").
      getDouble(timeout) max 0.05

    // --------------------------------------------------------------------- //

    if (config.hasChanged)
      config.save()
  }
}