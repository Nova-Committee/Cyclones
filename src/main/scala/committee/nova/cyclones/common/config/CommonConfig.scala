package committee.nova.cyclones.common.config

import net.minecraft.world.World
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

object CommonConfig {
  private var config: Configuration = _
  private var cycloneBlacklistDims: Array[String] = _
  private var cycloneRainStrengthThreshold: Float = _
  private var cycloneThunderStrengthThreshold: Float = _

  def init(e: FMLPreInitializationEvent): Unit = {
    config = new Configuration(e.getSuggestedConfigurationFile)
    config.load()
    cycloneBlacklistDims = config.getStringList("cycloneBlacklistDims", Configuration.CATEGORY_GENERAL, Array("-1", "1"),
      "Dimensions that won't generate cyclones")
    cycloneRainStrengthThreshold = config.getFloat("cycloneRainStrengthThreshold", Configuration.CATEGORY_GENERAL, .2F, .0F, 1.0F,
      "Only when the rain strength is not smaller than this value can cyclones occur")
    cycloneThunderStrengthThreshold = config.getFloat("cycloneThunderStrengthThreshold", Configuration.CATEGORY_GENERAL, .9F, .0F, 1.0F,
      "Only when the thunder strength is not smaller than this value can cyclones occur")
    config.save()
  }

  def isWorldInBlackList(world: World): Boolean = cycloneBlacklistDims.contains(world.provider.getDimension.toString)

  def canWeatherGenCyclones(world: World): Boolean = world.getRainStrength(1.0F) >= cycloneRainStrengthThreshold &&
    world.getThunderStrength(1.0F) > cycloneThunderStrengthThreshold
}
