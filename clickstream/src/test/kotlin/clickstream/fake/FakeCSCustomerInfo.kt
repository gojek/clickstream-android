package clickstream.fake

import clickstream.model.CSUserInfo

internal val fakeCustomerInfo =
    CSUserInfo(
        currentCountry = "ID",
        signedUpCountry = "ID",
        identity = 12345,
        email = "test@gmail.com"
    )