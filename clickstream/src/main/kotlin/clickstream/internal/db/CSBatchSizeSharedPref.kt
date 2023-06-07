package clickstream.internal.db

/***
 * Stores the batch size generated through the batching strategy
 * to be used while flushing in Shared prefs .The batching strategy determines the size
 * of batch based on the inflow of events and their byte size. This will
 * store the batch size and it is used while batching the events when we flush
 * events in background.
 */
internal interface CSBatchSizeSharedPref {
    suspend fun saveBatchSize(batchSize: Int)
    suspend fun getSavedBatchSize(): Int
}