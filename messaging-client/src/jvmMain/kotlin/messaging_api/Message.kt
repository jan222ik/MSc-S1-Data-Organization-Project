package messaging_api

import java.time.LocalDateTime

/**
 * Defines the schema for a message.
 * @param content String content of the message, it may be empty
 * @param timestamp states the time when the message was composed
 * @param author states the author that composed the message
 */
data class Message(val content: String, val timestamp: LocalDateTime, val author: Author)
