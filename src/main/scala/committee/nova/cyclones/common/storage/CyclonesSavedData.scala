package committee.nova.cyclones.common.storage

import committee.nova.cyclones.Cyclones
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.storage.WorldSavedData

object CyclonesSavedData {
  def get(world: World): CyclonesSavedData = {
    var data = world.getPerWorldStorage.getOrLoadData(classOf[CyclonesSavedData], Cyclones.MODID)
    if (data != null) return data.asInstanceOf[CyclonesSavedData]
    data = new CyclonesSavedData(Cyclones.MODID)
    world.getPerWorldStorage.setData(Cyclones.MODID, data)
    data.asInstanceOf[CyclonesSavedData]
  }
}

class CyclonesSavedData(name: String) extends WorldSavedData(name) {
  private var cycloneCountDown = 0
  private var cycloneTick = 0
  private var cycloneFinalTick = 0

  def isOnCountDown: Boolean = cycloneCountDown > 0

  def count: Boolean = {
    cycloneCountDown -= 1
    if (cycloneCountDown < 0) cycloneCountDown = 0
    markDirty()
    cycloneCountDown == 0
  }

  def tick: Boolean = {
    cycloneTick += 1
    if (cycloneTick >= cycloneFinalTick) {
      cycloneTick = 0
      cycloneFinalTick = 0
      markDirty()
      return true
    }
    markDirty()
    false
  }

  def isLeaving: Boolean = cycloneFinalTick > 0 && cycloneFinalTick - cycloneTick < 200

  def isActive: Boolean = cycloneTick < cycloneFinalTick

  def leave(world: World): Unit = {
    cycloneTick = 0 max (cycloneFinalTick - 199)
    markDirty()
  }

  def setCountDown(count: Int): Unit = {
    if (count < 0) return
    cycloneCountDown = count
    markDirty()
  }

  def setTick(tick: Int): Unit = {
    if (tick < 0) return
    cycloneTick = tick
    markDirty()
  }

  def setFinalTick(tick: Int): Unit = {
    if (tick < 0) return
    cycloneFinalTick = tick
    markDirty()
  }

  def getTransition: Float = {
    val remain = cycloneFinalTick - cycloneTick
    if (cycloneTick < 200) cycloneTick / 200.0F else if (remain < 200) remain / 200.0F else 1.0F
  }

  def getCountDown: Int = cycloneCountDown

  def getTick: Int = cycloneTick

  def getFinalTick: Int = cycloneFinalTick

  override def readFromNBT(tag: NBTTagCompound): Unit = {
    cycloneCountDown = tag.getInteger("countDown")
    cycloneTick = tag.getInteger("cycloneTick")
    cycloneFinalTick = tag.getInteger("cycloneFinalTick")
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setInteger("countDown", cycloneCountDown)
    tag.setInteger("cycloneTick", cycloneTick)
    tag.setInteger("cycloneFinalTick", cycloneFinalTick)
    tag
  }
}
