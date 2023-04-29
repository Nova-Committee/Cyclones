package committee.nova.cyclones

import committee.nova.cyclones.Cyclones.MODID
import committee.nova.cyclones.common.network.handler.NetworkHandler
import committee.nova.cyclones.implicits.Implicits.WorldImplicit
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(modid = MODID, useMetadata = true, modLanguage = "scala")
object Cyclones {
  final val MODID = "cyclones"
  final val cycloneInfluenceVisualSq = 100.0
  final val cycloneInfluenceLogicSq = 39.0

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = NetworkHandler.init(e)

  def isInfluencedByCycloneVisually(e1: Entity, e2: Entity): Boolean = {
    if (e1 == null || e2 == null || e1 == e2) return false
    val world = e1.world
    if (world != e2.world) return false
    val cyclone = world.getCyclone
    cyclone.isActive && e1.getDistanceSq(e2) > cycloneInfluenceVisualSq / (.01 + cyclone.getTransition)
  }

  def isInfluencedByCycloneLogically(e1: Entity, e2: Entity): Boolean = {
    if (e1 == null || e2 == null || e1 == e2) return false
    val world = e1.world
    if (world != e2.world) return false
    world.getCyclone.isActive && e1.getDistanceSq(e2) > cycloneInfluenceLogicSq
  }

  // TODO: Dimension Blacklist 
}
