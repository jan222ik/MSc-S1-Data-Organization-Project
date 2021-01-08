package messaging_api

import java.time.LocalDateTime

/**
 * Defies a Filter used to filter messages.
 * If a param value is null, it is ignored.
 * @param author defines a author to filter for.
 * @param startDateTime defines the inclusive start of the time interval
 * @param endDateTime defines the inclusive end of the time interval
 */
data class MessageFilter(val author: Author?, val startDateTime: LocalDateTime?, val endDateTime: LocalDateTime?)
