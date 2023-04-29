package committee.nova.cyclones.implicits

import committee.nova.cyclones.common.config.CommonConfig
import committee.nova.cyclones.common.storage.CyclonesSavedData
import net.minecraft.world.World

object Implicits {
  implicit class WorldImplicit(val world: World) {
    def getCyclone: CyclonesSavedData = CyclonesSavedData.get(world)

    def getCycloneRealRemainTime: Int = {
      val info = world.getWorldInfo
      (getCyclone.getFinalTick - getCyclone.getTick) min info.getRainTime min info.getThunderTime
    }

    def wontGenCyclone: Boolean = CommonConfig.isWorldInBlackList(world)
  }
}
