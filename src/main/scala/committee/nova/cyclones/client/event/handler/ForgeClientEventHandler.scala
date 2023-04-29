package committee.nova.cyclones.client.event.handler

import committee.nova.cyclones.Cyclones
import committee.nova.cyclones.implicits.Implicits.WorldImplicit
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity
import net.minecraftforge.client.event.{RenderLivingEvent, RenderPlayerEvent}
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@EventBusSubscriber(value = Array(Side.CLIENT))
object ForgeClientEventHandler {
  @SubscribeEvent
  def onRenderLiving(e: RenderLivingEvent.Pre[_ <: EntityLivingBase]): Unit = if (Cyclones.isInfluencedByCycloneVisually(Minecraft.getMinecraft.player, e.getEntity)) e.setCanceled(true)

  @SubscribeEvent
  def onRenderLivingSpecials(e: RenderLivingEvent.Specials.Pre[_ <: EntityLivingBase]): Unit = if (Cyclones.isInfluencedByCycloneVisually(Minecraft.getMinecraft.player, e.getEntity)) e.setCanceled(true)

  @SubscribeEvent
  def onRenderPlayer(e: RenderPlayerEvent.Pre): Unit = if (Cyclones.isInfluencedByCycloneVisually(Minecraft.getMinecraft.player, e.getEntityPlayer)) e.setCanceled(true)

  @SubscribeEvent
  def onRenderFog(e: FogDensity): Unit = {
    e.setCanceled(true)
    val cyclone = e.getEntity.world.getCyclone
    if (!cyclone.isActive) {
      e.setDensity(.001F)
      return
    }
    val remain = cyclone.getFinalTick - cyclone.getTick
    e.setDensity(.001F + cyclone.getTransition * .36F)
  }
}
