package li.cil.oc.client.gui

import li.cil.oc.common.container
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class Adapter(state: container.Adapter, playerInventory: Inventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name) {
}
