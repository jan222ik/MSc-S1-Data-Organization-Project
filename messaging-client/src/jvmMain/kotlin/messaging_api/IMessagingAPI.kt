package messaging_api

import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

interface IMessagingAPI : Closeable {
    /**
     * Defines a hot flow of all messages.
     */
    val messagesStateFlow: StateFlow<List<Message>>

    /**
     * Defines a hot flow of all authors that wrote a message.
     * Unique by their email.
     */
    val authorsStateFlow: StateFlow<List<Author>>

    /**
     * Publishes a Message to the server (aka. other clients)
     */
    fun sendMessage(msg: Message)
}
