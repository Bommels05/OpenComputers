package li.cil.oc.util

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

import scala.collection.mutable
import scala.language.implicitConversions

object ExtendedContainer {

  implicit def extendedContainer(container: Container): ExtendedInventory = new ExtendedInventory(container)

  class ExtendedInventory(val container: Container) extends mutable.IndexedSeq[ItemStack] {
    override def length = container.getContainerSize

    override def update(idx: Int, elem: ItemStack) = container.setItem(idx, elem)

    override def apply(idx: Int) = container.getItem(idx)
  }

}
