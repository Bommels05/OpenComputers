package li.cil.oc.common.capabilities;

import li.cil.oc.OpenComputers;
import li.cil.oc.api.internal.Colored;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class Capabilities {

    public static Capability<Colored> ColoredCapability = CapabilityManager.get(new CapabilityToken<>() {});

    public static Capability<Environment> EnvironmentCapability = CapabilityManager.get(new CapabilityToken<>() {});

    public static Capability<SidedEnvironment> SidedEnvironmentCapability = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void initialize(RegisterCapabilitiesEvent event) {
        OpenComputers.log().info("Initializing capabilities.");
        event.register(Colored.class);
        event.register(Environment.class);
        event.register(SidedEnvironment.class);
    }

    private Capabilities() {}
}
