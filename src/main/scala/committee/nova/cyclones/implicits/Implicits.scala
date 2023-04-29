package committee.nova.cyclones.implicits

import committee.nova.cyclones.common.storage.CyclonesSavedData
import net.minecraft.world.World

object Implicits {
  implicit class WorldImplicit(val world: World) {
    def getCyclone: CyclonesSavedData = CyclonesSavedData.get(world)
  }
}
