package li.cil.oc.api.driver.item;

import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.driver.DriverItem;
import net.minecraft.world.item.ItemStack;

/**
 * This interface can be added to item drivers to allow them to be picky
 * about their supported host environment.
 * <br>
 * This is useful for drivers for components that should only go into certain
 * environments, such as robot specific upgrades.
 */
public interface HostAware extends DriverItem {
    /**
     * Used to determine the item types this driver handles.
     * <br>
     * This is used to determine which driver to use for an item when it should
     * be installed in a computer. Note that the return value should not change
     * over time; if it does, though, an already installed component will not
     * be ejected, since this value is only checked when adding components.
     *
     * @param stack the item to check.
     * @param host  the type of host the environment would live in.
     * @return <code>true</code> if the item is supported; <code>false</code> otherwise.
     */
    boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host);
}
