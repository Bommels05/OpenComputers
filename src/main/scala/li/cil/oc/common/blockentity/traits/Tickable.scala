package li.cil.oc.common.blockentity.traits

import net.minecraft.world.level.block.entity.TickingBlockEntity

trait Tickable extends BlockEntity {
  def tick(): Unit = updateEntity()
  
}
