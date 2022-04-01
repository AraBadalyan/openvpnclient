package com.hundredstartups.openvpn.model

data class VpnData(
    val ipList: List<VpnEntity>
)

data class VpnEntity(
    val Ip: String?,
    val CountryCode: String?,
    val CityName: String?,
    val Region: String?,
    val Loc: String?,
    val Timezone: String?,
    val Conf: String?,
    val Speed: String?,
    val Time: Time?,
    val Traffic: Traffic?
)

data class Time(
    var endTime: Long?,
)

data class Traffic(
    val cur: Long?,
    val all: Long?
)
