package city_queries

import kotlinx.coroutines.runBlocking
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.match
import org.litote.kmongo.or
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.regex

/**
 * Activity exercise mongodb.
 */
fun main() {
    // Create client
    val client: CoroutineClient = KMongo.createClient().coroutine
    // Get database test with town collection in it
    val database = client.getDatabase("test")
    // Get town collection
    val towns = database.getCollection<Town>("towns")

    runBlocking {
        //– Select a town via a case-insensitive regular expression containing the string vie.
        towns.aggregate<Town>(
            listOf(
                match(Town::name regex ".*vie.*".toRegex(RegexOption.IGNORE_CASE))
            )
        ).toList().forEach(::println)

        //– Find all cities whose names contain an z or are famous for Essen.
        towns.aggregate<Town>(
            listOf(
                match(
                    or(
                        Town::name regex ".*z.*",
                        Town::famous_for contains "Essen"
                    )
                )
            )
        ).toList().forEach(::println)
    }

}

/**
 * Defines params for a subset of towns collections fields.
 */
data class Town(val name: String, val famous_for: List<String>)
