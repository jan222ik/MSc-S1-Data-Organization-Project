package messaging_api.impl

import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messaging_api.*


object DatabaseImpl : IInspectionAPI {

    private val redisHandler = LettuceHandler()
    private val mongoHandler = MongoHandler()

    private val internalMessageStateFlow = MutableStateFlow<List<Message>>(listOf())
    private val internalAuthorsStateFlow = MutableStateFlow<List<Author>>(listOf())

    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessageStateFlow
    override val authorsStateFlow: StateFlow<List<Author>>
        get() = internalAuthorsStateFlow

    init {
        GlobalScope.launch {
            internalMessageStateFlow.collect {
                internalAuthorsStateFlow.emit(extractUsers(it))
                internalHasUpdatesStateFlow.emit(true)
            }
        }
        runBlocking {
            val hist = mongoHandler.getHistory()
            val l = listOf(
                *hist.toTypedArray(),
                *internalMessageStateFlow.value.toTypedArray()
            )
            internalMessageStateFlow.emit(l)
        }
        redisHandler.connect(
            onNextMessage = {
                val gson = Gson()
                val msg = gson.fromJson(it, Message::class.java)
                val l = listOf(*internalMessageStateFlow.value.toTypedArray(), msg)
                internalMessageStateFlow.emit(l)
            }
        )
    }

    private fun extractUsers(msgs: List<Message>): List<Author> {
        val map = mutableMapOf<String, Author>()
        map.putAll(msgs.map { it.author.email to it.author })
        return map.values.toList()
    }

    override suspend fun sendMessage(msg: Message) {
        redisHandler.sendMessage(msg)
        mongoHandler.pushMessage(msg)
    }

    override fun close() {
        redisHandler.close()
    }

    private val internalFilteredMessageStateFlow = MutableStateFlow<List<Message>>(listOf())
    private val internalHasUpdatesStateFlow = MutableStateFlow(false)

    override val filteredMessagesStateFlow: StateFlow<List<Message>>
        get() = internalFilteredMessageStateFlow
    override val hasUpdates: StateFlow<Boolean>
        get() = internalHasUpdatesStateFlow

    override suspend fun applyFilter(filter: MessageFilter) {
        println("Apply Filter")
        val list = mongoHandler.filterMessages(filter)
        internalFilteredMessageStateFlow.emit(list)
        internalHasUpdatesStateFlow.emit(false)
    }


}
