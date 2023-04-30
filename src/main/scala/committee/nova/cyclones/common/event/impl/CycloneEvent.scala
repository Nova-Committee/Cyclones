package committee.nova.cyclones.common.event.impl

import net.minecraft.world.World
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.Cancelable

object CycloneEvent {
  @Cancelable
  class Start$Pre(world: World) extends Start(world) {
    private var shouldNotify = false

    override def isCancelable: Boolean = true

    def setDuration(duration: Int): Unit = {
      if (duration <= 0) return
      this.duration = duration
    }

    def setCount(count: Int): Unit = {
      if (count <= 0) return
      this.count = count
    }

    def willNotify: Boolean = shouldNotify

    def setWillNotify(notify: Boolean): Unit = shouldNotify = notify
  }

  class Start$Post(world: World) extends Start(world)

  class Notify(world: World, count: Int) extends CycloneEvent(world) {
    def getCount: Int = count
  }

  class Start(world: World) extends CycloneEvent(world) {
    protected var count: Int = 100 + world.rand.nextInt(201)
    protected var duration: Int = 24000 + world.rand.nextInt(48000)

    def getDuration: Int = duration

    def getCount: Int = count
  }

  class AboutToLeave(world: World) extends CycloneEvent(world)

  class Stop(world: World) extends CycloneEvent(world)
}

class CycloneEvent(world: World) extends WorldEvent(world)
