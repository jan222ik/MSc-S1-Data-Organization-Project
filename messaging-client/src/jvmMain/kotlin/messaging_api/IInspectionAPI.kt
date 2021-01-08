package messaging_api

import kotlinx.coroutines.flow.StateFlow

interface IInspectionAPI : IMessagingAPI {
    /**
     * Defines a hot flow of all messages matching the filter.
     */
    val filteredMessagesStateFlow: StateFlow<List<Message>>

    /**
     * Defines a hot flow to indicate that new messages are available and not yet filtered.
     */
    val hasUpdates: StateFlow<Boolean>

    /**
     * Applies given filter to all messages sent.
     * @param filter: provided Filter
     */
    suspend fun applyFilter(filter: MessageFilter)

    /**
     * Defines a hot flow of all entries in the collection 'senderstats'.
     */
    val senderStatsStateFlow: StateFlow<List<SenderStat>>

    /**
     * Initiates the mapReduce function to populate the 'senderstats' collection.
     */
    fun calculateSenderStats()
}
