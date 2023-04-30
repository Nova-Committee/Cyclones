package committee.nova.cyclones.common.network.msg

import committee.nova.cyclones.common.init.SoundInit
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.{ISound, PositionedSoundRecord}
import net.minecraft.util.SoundCategory
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import net.minecraftforge.fml.relauncher.Side

object CycloneStartSoundMessage {
  class Handler extends IMessageHandler[CycloneStartSoundMessage, IMessage] {
    override def onMessage(message: CycloneStartSoundMessage, ctx: MessageContext): IMessage = {
      if (ctx.side != Side.CLIENT) return null
      val mc = Minecraft.getMinecraft
      val handler = mc.getSoundHandler
      mc.addScheduledTask(new Runnable {
        override def run(): Unit = handler.playSound(new PositionedSoundRecord(SoundInit.cycloneStart.getRegistryName, SoundCategory.WEATHER,
          1.0F, 1.0F, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F))
      })
      null
    }
  }
}

class CycloneStartSoundMessage extends IMessage {
  override def fromBytes(buf: ByteBuf): Unit = {}

  override def toBytes(buf: ByteBuf): Unit = {}
}
