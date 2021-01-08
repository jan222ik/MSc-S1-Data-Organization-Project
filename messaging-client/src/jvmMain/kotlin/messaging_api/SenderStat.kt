package messaging_api

/**
 * Defines the return type of a 'mapReduce' produced collection.
 * @param _id defines the key of the map operation.
 * @param value defies the value of the mapReduce operation.
 */
data class SenderStat(
    val _id: Author,
    val value: Int
)

// Extension Property as named getter.
val SenderStat.author
    get() = this._id

// Extension Property as named getter.
val SenderStat.messageCount
    get() = this.value
