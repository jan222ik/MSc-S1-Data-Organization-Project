package messaging_api

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
