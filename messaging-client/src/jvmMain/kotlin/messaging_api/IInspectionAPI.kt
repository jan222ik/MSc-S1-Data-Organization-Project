package messaging_api

import kotlinx.coroutines.flow.StateFlow

interface IInspectionAPI : IMessagingAPI {
    val filteredMessagesStateFlow: StateFlow<List<Message>>
    val hasUpdates: StateFlow<Boolean>
    suspend fun applyFilter(filter: MessageFilter)
}
