package committee.nova.cyclones.common.config

import net.minecraft.world.World
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

object CommonConfig {
  private var config: Configuration = _
  private var cycloneBlacklistDims: Array[String] = _

  def init(e: FMLPreInitializationEvent): Unit = {
    config = new Configuration(e.getSuggestedConfigurationFile)
    config.load()
    cycloneBlacklistDims = config.getStringList("cycloneBlacklistDims", Configuration.CATEGORY_GENERAL, Array("-1", "1"),
      "Dimensions that won't generate cyclones")
    config.save()
  }

  def isWorldInBlackList(world: World): Boolean = cycloneBlacklistDims.contains(world.provider.getDimension.toString)
}
