package committee.nova.cyclones.common.event.handler

import committee.nova.cyclones.Cyclones
import committee.nova.cyclones.common.event.impl.CycloneEvent
import committee.nova.cyclones.common.event.impl.CycloneEvent.{Start$Post, Start$Pre}
import committee.nova.cyclones.common.init.SoundInit
import committee.nova.cyclones.common.network.handler.NetworkHandler
import committee.nova.cyclones.common.network.msg.CycloneStatusSyncMessage
import committee.nova.cyclones.implicits.Implicits.WorldImplicit
import net.minecraft.entity.EntityLiving
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.SoundCategory
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent
import net.minecraftforge.event.entity.player.PlayerEvent.Visibility
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{Phase, WorldTickEvent}

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@EventBusSubscriber
object ForgeEventHandler {
  @SubscribeEvent
  def onVisibility(e: Visibility): Unit = if (e.getEntityPlayer.world.getCyclone.isActive) e.modifyVisibility(0.8)

  @SubscribeEvent
  def onSetTarget(e: LivingSetAttackTargetEvent): Unit = {
    e.getEntityLiving match {
      case l: EntityLiving =>
        if (Cyclones.isInfluencedByCyclone(l, e.getTarget)) l.setAttackTarget(null)
      case _ =>
    }
  }

  @SubscribeEvent
  def onTick(e: WorldTickEvent): Unit = {
    val world = e.world
    val cyclone = world.getCyclone
    if (e.phase == Phase.START) {
      val couldCause = world.isRaining && world.isThundering
      if (!couldCause) {
        if (cyclone.isActive) cyclone.stop(world)
        return
      }
      if (cyclone.isOnCountDown) {
        if (!cyclone.count) return
        val pre = new Start$Pre(world)
        if (MinecraftForge.EVENT_BUS.post(pre)) {
          cyclone.setCountDown(pre.getCount)
          if (pre.willNotify) MinecraftForge.EVENT_BUS.post(new CycloneEvent.Notify(world, cyclone.getCountDown))
          return
        }
        cyclone.setTick(pre.getDuration)
        MinecraftForge.EVENT_BUS.post(new Start$Post(world))
        return
      }
      if (cyclone.isActive) {
        if (cyclone.tick) MinecraftForge.EVENT_BUS.post(new CycloneEvent.Stop(world))
        return
      }
      if (world.rand.nextBoolean() || world.rand.nextBoolean()) return
      cyclone.setCountDown(200)
      MinecraftForge.EVENT_BUS.post(new CycloneEvent.Notify(world, cyclone.getCountDown))
    } else {
      val msg = new CycloneStatusSyncMessage
      val dim = world.provider.getDimension
      msg.setDim(dim)
      msg.setCount(cyclone.getCountDown)
      msg.setTick(cyclone.getTick)
      NetworkHandler.instance.sendToDimension(msg, dim)
    }
  }

  @SubscribeEvent
  def onCycloneStart(e: CycloneEvent.Start$Post): Unit = {
    e.getWorld.getMinecraftServer.getPlayerList.getPlayers.asScala.foreach(p => {
      p.sendStatusMessage(new TextComponentTranslation("msg.cyclones.start"), true)
      p.connection.sendPacket(new SPacketSoundEffect(SoundInit.cycloneStart, SoundCategory.WEATHER, p.posX, p.posY, p.posZ, 1.0F, 1.0F))
    })
  }

  @SubscribeEvent
  def onCycloneNotify(e: CycloneEvent.Notify): Unit = {
    e.getWorld.getMinecraftServer.getPlayerList.getPlayers.asScala.foreach(p => p.sendStatusMessage(new TextComponentTranslation("msg.cyclones.notify", e.getCount.toString), true))
  }

  @SubscribeEvent
  def onCycloneStop(e: CycloneEvent.Stop): Unit = {
    e.getWorld.getMinecraftServer.getPlayerList.getPlayers.asScala.foreach(p => p.sendStatusMessage(new TextComponentTranslation("msg.cyclones.stop"), true))
  }
}
