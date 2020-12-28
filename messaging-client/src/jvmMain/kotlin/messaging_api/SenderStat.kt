package messaging_api

data class SenderStat(
    val _id: Author,
    val value: Int
)

val SenderStat.author
    get() = this._id

val SenderStat.messageCount
    get() = this.value
