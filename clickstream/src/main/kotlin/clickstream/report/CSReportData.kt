package clickstream.report

/**
 * data class for representing report related data.
 * */
public sealed class CSReportData(public open val date: String) {

    /**
     * Batch is acknowledged successfully by server.
     * */
    public class CSSuccess(override val date: String, public val batchId: String) :
        CSReportData(date)

    /**
     * Failed to acknowledge batch by server.
     * */
    public class CSFailure(
        override val date: String,
        public val batchId: String,
        public val exception: Throwable
    ) : CSReportData(date)

    /**
     * Duplicate event sent by clickstream sdk.
     * */
    public class CSDuplicateEvents(
        override val date: String,
        public val batchId: String,
        public val guid: String
    ) :
        CSReportData(date)

    /**
     * Text message.
     * */
    public class CSMessage(override val date: String, public val message: String) :
        CSReportData(date)
}