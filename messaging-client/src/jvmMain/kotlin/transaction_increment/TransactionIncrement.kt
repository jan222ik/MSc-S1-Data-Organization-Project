package transaction_increment

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import io.lettuce.core.TransactionResult
import io.lettuce.core.api.async.RedisAsyncCommands

fun main() {
    val client = RedisClient.create("redis://redis@localhost")
    val async: RedisAsyncCommands<String, String> = client.connect().async()
    val multi: RedisFuture<String> = async.multi()
    val set: RedisFuture<String> = async.set("inc", "1")
    val incOne: RedisFuture<Long> = async.incr("inc")
    val incTwo: RedisFuture<Long> = async.incr("inc")
    val incThree: RedisFuture<Long> = async.incr("inc")
    val exec: RedisFuture<TransactionResult> = async.exec()
    // TODO left unused vals in for type information, can be replaced by function calls without assignment
    println(exec.get().forEach(::println))
    client.shutdown()
}


