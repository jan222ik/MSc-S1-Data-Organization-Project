package messaging_api

import kotlinx.coroutines.flow.StateFlow

interface IMessagingAPI {
    val messagesStateFlow : StateFlow<List<Message>>
    suspend fun sendMessage(msg: Message)
    // TODO Usage Statistics -> how many messages have users composed.
}
