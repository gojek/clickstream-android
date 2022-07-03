package clickstream.fake

import clickstream.api.CSMetaProvider
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.model.CSHealthEventDTO

internal fun fakeCSHealthEventDTOs(
    csMetaProvider: CSMetaProvider
): List<CSHealthEventDTO> {
    val list = mutableListOf<CSHealthEventDTO>()

    CSHealthEventDTO(
        eventName = CSEventNamesConstant.Flushed.ClickStreamEventReceived.value,
        eventType = CSEventTypesConstant.AGGREGATE,
        eventGuid = "1",
        appVersion = csMetaProvider.app.version,
        timeToConnection = 2,
        error = "no error",
        eventBatchGuid = "1"
    ).let(list::add)

    CSHealthEventDTO(
        eventName = CSEventNamesConstant.Flushed.ClickStreamEventReceived.value,
        eventType = CSEventTypesConstant.AGGREGATE,
        eventGuid = "2",
        appVersion = csMetaProvider.app.version,
        timeToConnection = 3,
        error = "no error",
        eventBatchGuid = "2"
    ).let(list::add)

    CSHealthEventDTO(
        eventName = CSEventNamesConstant.Flushed.ClickStreamEventReceived.value,
        eventType = CSEventTypesConstant.AGGREGATE,
        eventGuid = "3",
        appVersion = csMetaProvider.app.version,
        timeToConnection = 4,
        error = "no error",
        eventBatchGuid = "3"
    ).let(list::add)

    return list
}