package com.shilapi.xcertplay.airplay

import java.nio.file.Files
import org.junit.Assert.*
import org.junit.Test

class VideoResourceStoreTest {
    @Test fun byteAndFileQuotasAreEnforcedAndCloseIsIdempotent() {
        val root = Files.createTempDirectory("diplay-resource-quota-").toFile()
        try {
            val store = VideoResourceStore(root, byteLimit = 8, fileLimit = 2)
            val first = store.save(ByteArray(5))
            assertThrows(java.io.IOException::class.java) { store.save(ByteArray(4)) }
            val second = store.save(ByteArray(3))
            assertThrows(java.io.IOException::class.java) { store.save(ByteArray(0)) }
            first.close(); first.close(); second.close()
            assertEquals(0L, store.retainedBytes())
            store.save(ByteArray(8)).use { assertTrue(it.file.exists()) }
        } finally { root.deleteRecursively() }
    }

    @Test fun aResourceIsSpooledAndUnneededLargeMetadataIsNotPostedToTheUi() {
        val root = Files.createTempDirectory("diplay-resource-spool-").toFile()
        try {
            val store = VideoResourceStore(root)
            val bytes = ByteArray(512 * 1024) { it.toByte() }
            val message = mapOf("type" to "unhandledURL", "unused" to bytes,
                "response" to mapOf("FCUP_Response_RequestID" to 5L, "FCUP_Response_StatusCode" to 200L,
                    "FCUP_Response_Data" to bytes, "unused" to bytes))
            val prepared = spoolVideoResource(message, store)
            assertFalse(prepared.containsKey("unused"))
            val response = prepared["response"] as Map<*, *>
            assertFalse(response.containsKey("unused"))
            (response["FCUP_Response_Data"] as VideoResourceFile).use { assertArrayEquals(bytes, it.file.readBytes()) }
            assertEquals(0L, store.retainedBytes())
        } finally { root.deleteRecursively() }
    }

    @Test fun aFullCacheRejectsOnlyTheResourceAndDoesNotQueueItsBytes() {
        val root = Files.createTempDirectory("diplay-resource-full-").toFile()
        try {
            val store = VideoResourceStore(root, byteLimit = 1)
            val message = mapOf("type" to "unhandledURL", "response" to mapOf(
                "FCUP_Response_RequestID" to 5L, "FCUP_Response_Data" to ByteArray(512 * 1024)))
            val response = spoolVideoResource(message, store)["response"] as Map<*, *>
            assertEquals(507L, response["FCUP_Response_StatusCode"])
            assertEquals(0, (response["FCUP_Response_Data"] as ByteArray).size)
            assertEquals(0L, store.retainedBytes())
        } finally { root.deleteRecursively() }
    }
}
