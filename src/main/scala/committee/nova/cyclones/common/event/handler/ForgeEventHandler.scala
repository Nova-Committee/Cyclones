package committee.nova.cyclones.common.event.handler

import com.google.common.base.Predicate
import committee.nova.cyclones.Cyclones
import committee.nova.cyclones.common.event.impl.CycloneEvent
import committee.nova.cyclones.common.event.impl.CycloneEvent.{Start$Post, Start$Pre}
import committee.nova.cyclones.common.network.handler.NetworkHandler
import committee.nova.cyclones.common.network.msg.{CycloneStartSoundMessage, CycloneStatusSyncMessage}
import committee.nova.cyclones.implicits.Implicits.WorldImplicit
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.{EntityLiving, EntityLivingBase}
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.text.{Style, TextComponentTranslation, TextFormatting}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent
import net.minecraftforge.event.entity.player.PlayerEvent.Visibility
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{Phase, WorldTickEvent}
import net.minecraftforge.fml.relauncher.Side

import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import scala.collection.mutable

@EventBusSubscriber
object ForgeEventHandler {

  def getAlways[T]: Predicate[_ >: T] = new Predicate[T] {
    override def apply(input: T): Boolean = true
  }

  @SubscribeEvent
  def onVisibility(e: Visibility): Unit = if (e.getEntityPlayer.world.getCyclone.isActive) e.modifyVisibility(0.8)

  @SubscribeEvent
  def onSetTarget(e: LivingSetAttackTargetEvent): Unit = {
    e.getEntityLiving match {
      case l: EntityLiving =>
        if (Cyclones.isInfluencedByCycloneLogically(l, e.getTarget)) l.setAttackTarget(null)
      case _ =>
    }
  }

  @SubscribeEvent
  def onTargetCheck(e: WorldTickEvent): Unit = {
    if (e.side != Side.SERVER || e.phase == Phase.START) return
    val world = e.world
    if (world.wontGenCyclone || !world.getCyclone.isActive) return
    if (world.getWorldTime % 100L == 0L) {
      val aabbs = mutable.Buffer[AxisAlignedBB]()
      world.getPlayers[EntityPlayerMP](classOf[EntityPlayerMP], getAlways[EntityPlayerMP]).asScala.foreach(p => aabbs.+=(new AxisAlignedBB(p.getPosition.add(50, 50, 50), p.getPosition.add(-50, -50, -50))))
      for (aabb <- aabbs) world.getEntitiesWithinAABB[EntityLiving](classOf[EntityLiving], aabb, getAlways[EntityLiving]).asScala.foreach(l => {
        l.getRevengeTarget match {
          case e: EntityLivingBase => if (Cyclones.isInfluencedByCycloneLogically(l, e)) l.setRevengeTarget(null)
          case _ =>
        }
        l.getAttackTarget match {
          case e: EntityLivingBase => if (Cyclones.isInfluencedByCycloneLogically(l, e)) l.setAttackTarget(null)
          case _ =>
        }
      })
    }
  }

  @SubscribeEvent
  def onCycloneTick(e: WorldTickEvent): Unit = {
    if (e.side != Side.SERVER) return
    val world = e.world
    if (world.wontGenCyclone) return
    val cyclone = world.getCyclone
    if (e.phase == Phase.START) {
      if (!world.weatherCanGenCyclones) {
        if (!cyclone.isActive) return
        if (!cyclone.isLeaving) cyclone.leave()
        else {
          if (cyclone.getRemainTick == 198) MinecraftForge.EVENT_BUS.post(new CycloneEvent.AboutToLeave(world))
          if (cyclone.tick) MinecraftForge.EVENT_BUS.post(new CycloneEvent.Stop(world))
        }
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
        cyclone.setFinalTick(pre.getDuration)
        MinecraftForge.EVENT_BUS.post(new Start$Post(world))
        return
      }
      if (cyclone.isActive) {
        if (cyclone.getRemainTick == 200) MinecraftForge.EVENT_BUS.post(new CycloneEvent.AboutToLeave(world))
        if (cyclone.tick) MinecraftForge.EVENT_BUS.post(new CycloneEvent.Stop(world))
      }
    } else {
      val msg = new CycloneStatusSyncMessage
      val dim = world.provider.getDimension
      msg.setDim(dim)
      msg.setCount(cyclone.getCountDown)
      msg.setTick(cyclone.getTick)
      msg.setFinalTick(cyclone.getFinalTick)
      NetworkHandler.instance.sendToDimension(msg, dim)
    }
  }

  @SubscribeEvent
  def onCycloneGen(e: WorldTickEvent): Unit = {
    if (e.side != Side.SERVER) return
    if (e.phase != Phase.END) return
    val world = e.world
    val cyclone = world.getCyclone
    if (!world.isRaining || !world.isThundering || cyclone.isOnCountDown || cyclone.isActive) return
    if (world.rand.nextBoolean() || world.rand.nextBoolean()) return
    val cd = 100 + world.rand.nextInt(201)
    MinecraftForge.EVENT_BUS.post(new CycloneEvent.Notify(world, cd))
    cyclone.setCountDown(cd)
  }

  @SubscribeEvent
  def onCycloneStart(e: CycloneEvent.Start$Post): Unit = {
    val world = e.getWorld
    world.getEntities[EntityLiving](classOf[EntityLiving], new Predicate[EntityLiving] {
      override def apply(input: EntityLiving): Boolean = world.getChunk(input.getPosition).isLoaded
    }).asScala.foreach(l => {
      l.setRevengeTarget(null)
      l.setAttackTarget(null)
    })
    world.getMinecraftServer.getPlayerList.getPlayers.asScala.foreach(p => {
      p.sendStatusMessage(new TextComponentTranslation("msg.cyclones.start")
        .setStyle(new Style().setColor(TextFormatting.RED)), true)
      //p.connection.sendPacket(new SPacketSoundEffect(SoundInit.cycloneStart, SoundCategory.WEATHER, p.posX, p.posY, p.posZ, 1.0F, 1.0F))
    })
    NetworkHandler.instance.sendToDimension(new CycloneStartSoundMessage, world.provider.getDimension)
  }

  @SubscribeEvent
  def onCycloneNotify(e: CycloneEvent.Notify): Unit = {
    val count = String.format("%.1f", (e.getCount / 20.0).asInstanceOf[java.lang.Double])
    e.getWorld.getMinecraftServer.getPlayerList.getPlayers.asScala.foreach(p => p.sendStatusMessage(new TextComponentTranslation("msg.cyclones.notify",
      count).setStyle(new Style().setColor(TextFormatting.YELLOW)), true))
  }

  @SubscribeEvent
  def onCycloneAboutToLeave(e: CycloneEvent.AboutToLeave): Unit = {
    e.getWorld.getMinecraftServer.getPlayerList.getPlayers.asScala.foreach(p => p.sendStatusMessage(new TextComponentTranslation("msg.cyclones.stop")
      .setStyle(new Style().setColor(TextFormatting.GREEN)), true))
  }
}
