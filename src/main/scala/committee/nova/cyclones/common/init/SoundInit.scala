package committee.nova.cyclones.common.init

import committee.nova.cyclones.Cyclones
import net.minecraft.util.{ResourceLocation, SoundEvent}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@EventBusSubscriber
object SoundInit {
  val cycloneStart: SoundEvent = new SoundEvent(new ResourceLocation(Cyclones.MODID, "cyclone_start")).setRegistryName(new ResourceLocation(Cyclones.MODID, "cyclone_start"))

  @SubscribeEvent
  def onSoundRegistry(e: RegistryEvent.Register[SoundEvent]): Unit = {
    val reg = e.getRegistry
    reg.register(cycloneStart)
  }
}
