package committee.nova.cyclones.common.network.msg

import committee.nova.cyclones.implicits.Implicits.WorldImplicit
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import net.minecraftforge.fml.relauncher.Side

object CycloneStatusSyncMessage {
  class Handler extends IMessageHandler[CycloneStatusSyncMessage, IMessage] {
    override def onMessage(message: CycloneStatusSyncMessage, ctx: MessageContext): IMessage = {
      if (ctx.side != Side.CLIENT) return null
      val world = Minecraft.getMinecraft.world
      if (world == null) return null
      if (message.dim != world.provider.getDimension) return null
      val cyclone = world.getCyclone
      cyclone.setCountDown(message.count)
      cyclone.setTick(message.tick)
      cyclone.setFinalTick(message.finalTick)
      null
    }
  }
}

class CycloneStatusSyncMessage extends IMessage {
  private var dim = 0
  private var count = 0
  private var tick = 0
  private var finalTick = 0

  def setDim(dim: Int): Unit = this.dim = dim

  def setCount(count: Int): Unit = this.count = count

  def setTick(tick: Int): Unit = this.tick = tick

  def setFinalTick(finalTick: Int): Unit = this.finalTick = finalTick

  override def fromBytes(buf: ByteBuf): Unit = {
    dim = buf.readInt()
    count = buf.readInt()
    tick = buf.readInt()
    finalTick = buf.readInt()
  }

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(dim)
    buf.writeInt(count)
    buf.writeInt(tick)
    buf.writeInt(finalTick)
  }
}
