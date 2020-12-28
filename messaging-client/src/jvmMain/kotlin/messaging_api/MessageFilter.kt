package messaging_api

import java.time.LocalDateTime

data class MessageFilter(val author: Author?, val startDateTime: LocalDateTime?, val endDateTime: LocalDateTime?)
