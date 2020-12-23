package messaging_api

import java.time.LocalDateTime

data class Message(val content: String, val timestamp: LocalDateTime, val author: Author)
