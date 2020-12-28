package messaging_api.impl

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import messaging_api.Author
import messaging_api.IMessagingAPI
import messaging_api.Message
import java.time.LocalDateTime

val authors = listOf(
    Author("Holger", "holger@gmail.com"),
    Author("Hans", "hans@entertainment.de"),
    Author("Hugo", "hugo@boss.is"),
    Author("Alfons", "alfons@freemail.com")
)


val history = listOf(
    Message("Hello, my name is Holger", LocalDateTime.now(), authors[0]),
    Message("I'm Hans", LocalDateTime.now(), authors[1]),
    Message("Nice to meet you all, call me Hugo", LocalDateTime.now(), authors[2]),
    Message(
        "Does everyone have the agenda for today?",
        LocalDateTime.now(),
        authors[0]
    ),
    Message("If not please message me", LocalDateTime.now(), authors[0]),
    Message(
        "Hey.\nAlfons here. I would need a copy.",
        LocalDateTime.now(),
        authors[3]
    )
)

object DemoMsg : IMessagingAPI {
    private val internalMessagesStateFlow = MutableStateFlow(history)
    private val internalAuthorStateFlow = MutableStateFlow(authors)

    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessagesStateFlow

    override val authorsStateFlow: StateFlow<List<Author>>
        get() = internalAuthorStateFlow

    override suspend fun sendMessage(msg: Message) {
        val l = listOf(*internalMessagesStateFlow.value.toTypedArray(), msg)
        internalMessagesStateFlow.emit(l)
    }

    init {
        GlobalScope.launch {
            internalMessagesStateFlow.collect {
                internalAuthorStateFlow.emit(
                    extractUsers(it)
                )
            }
        }
    }

    private fun extractUsers(msgs: List<Message>): List<Author> {
        val map = mutableMapOf<String, Author>()
        map.putAll(msgs.map { it.author.email to it.author })
        return map.values.toList()
    }

    override fun close() {

    }
}
