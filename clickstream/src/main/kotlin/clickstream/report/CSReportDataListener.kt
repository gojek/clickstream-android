package clickstream.report

/**
 * Interface to communicate report related data to client app.
 * */
public interface CSReportDataListener {

    /**
     * Callback for report data as [CSReportData].
     * */
    public fun onNewData(tag: String, data: CSReportData)
}