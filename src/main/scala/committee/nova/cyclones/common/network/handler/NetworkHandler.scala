package committee.nova.cyclones.common.network.handler

import committee.nova.cyclones.Cyclones
import committee.nova.cyclones.common.network.msg.CycloneStatusSyncMessage
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, SimpleNetworkWrapper}
import net.minecraftforge.fml.relauncher.Side

object NetworkHandler {
  val instance: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Cyclones.MODID)

  var nextID: Int = 0

  def init(e: FMLPreInitializationEvent): Unit = {
    registerMessage(classOf[CycloneStatusSyncMessage.Handler], classOf[CycloneStatusSyncMessage], Side.CLIENT)
  }

  def registerMessage[REQ <: IMessage, REPLY <: IMessage](msgHandler: Class[_ <: IMessageHandler[REQ, REPLY]], requestMsgType: Class[REQ], side: Side): Unit = {
    nextID += 1
    instance.registerMessage(msgHandler, requestMsgType, nextID, side)
  }
}
