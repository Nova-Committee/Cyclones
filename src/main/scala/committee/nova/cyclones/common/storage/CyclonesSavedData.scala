package committee.nova.cyclones.common.storage

import committee.nova.cyclones.Cyclones
import committee.nova.cyclones.common.event.impl.CycloneEvent
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.common.MinecraftForge

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
  private var cycloneCountDown = 200
  private var cycloneTick = 0

  def isOnCountDown: Boolean = cycloneCountDown > 0

  def count: Boolean = {
    cycloneCountDown -= 1
    if (cycloneCountDown < 0) cycloneCountDown = 0
    markDirty()
    cycloneCountDown == 0
  }

  def tick: Boolean = {
    cycloneTick -= 1
    if (cycloneTick < 0) cycloneTick = 0
    markDirty()
    cycloneTick == 0
  }

  def isActive: Boolean = cycloneTick > 0

  def stop(world: World): Unit = {
    cycloneCountDown = 200
    cycloneTick = 0
    MinecraftForge.EVENT_BUS.post(new CycloneEvent.Stop(world))
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

  def getCountDown: Int = cycloneCountDown

  def getTick: Int = cycloneTick

  override def readFromNBT(tag: NBTTagCompound): Unit = {
    cycloneCountDown = tag.getInteger("countDown")
    cycloneTick = tag.getInteger("cycloneTick")
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setInteger("countDown", cycloneCountDown)
    tag.setInteger("cycloneTick", cycloneTick)
    tag
  }
}
