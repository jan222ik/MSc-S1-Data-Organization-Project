package messaging_api.impl

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messaging_api.Author
import messaging_api.IInspectionAPI
import messaging_api.LettuceHandler
import messaging_api.Message
import messaging_api.MessageFilter
import messaging_api.MongoHandler
import messaging_api.SenderStat


object DatabaseImpl : IInspectionAPI {

    private val redisHandler = LettuceHandler()
    private val mongoHandler = MongoHandler()

    // Flow of all Messages.
    private val internalMessageStateFlow = MutableStateFlow<List<Message>>(emptyList())
    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessageStateFlow

    // Flow of all Authors that composed at least one Message.
    private val internalAuthorsStateFlow = MutableStateFlow<List<Author>>(emptyList())
    override val authorsStateFlow: StateFlow<List<Author>>
        get() = internalAuthorsStateFlow

    // Flow of all Messages satisfying the applied Filter.
    private val internalFilteredMessageStateFlow = MutableStateFlow<List<Message>>(emptyList())
    override val filteredMessagesStateFlow: StateFlow<List<Message>>
        get() = internalFilteredMessageStateFlow

    // Flow of the latest mapReduce of SenderStats.
    private val internalSenderStatStateFlow = MutableStateFlow<List<SenderStat>>(emptyList())
    override val senderStatsStateFlow: StateFlow<List<SenderStat>>
        get() = internalSenderStatStateFlow

    // Flow that indicates that there are new Messages not considered by the inspector use-cases FilteredMessages or SenderStat.
    private val internalHasUpdatesStateFlow = MutableStateFlow(false)
    override val hasUpdates: StateFlow<Boolean>
        get() = internalHasUpdatesStateFlow

    init {
        GlobalScope.launch(Dispatchers.IO) {
            // Create Subscriber on IO ThreadPool to observe incoming messages and update connected StateFlows.
            internalMessageStateFlow.collect {
                // Update Users that composed at least one Message.
                internalAuthorsStateFlow.emit(extractUsers(it))
                // Update hasUpdate
                internalHasUpdatesStateFlow.emit(true)
            }
        }
        runBlocking {
            // Block Main Thread to avoid Concurrency Problems (Race Conditions).
            //       - Should be implemented in a different way in production.

            // Get History from MongoDB.
            val hist = mongoHandler.getHistory()
            val messageList = listOf(
                *hist.toTypedArray(),
                *internalMessageStateFlow.value.toTypedArray() // TODO Needed ?
            )
            // Update Message StateFlow.
            internalMessageStateFlow.emit(messageList)
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

    override suspend fun applyFilter(filter: MessageFilter) {
        println("Apply Filter")
        val list = mongoHandler.filterMessages(filter)
        internalFilteredMessageStateFlow.emit(list)
        internalHasUpdatesStateFlow.emit(false)
    }


    override fun calculateSenderStats() {
        GlobalScope.launch(Dispatchers.IO) {
            val senderStats = mongoHandler.calculateSenderStats()
            internalSenderStatStateFlow.emit(senderStats)
            internalHasUpdatesStateFlow.emit(false)
        }
    }


    override fun close() {
        mongoHandler.close()
        redisHandler.close()
    }

}
