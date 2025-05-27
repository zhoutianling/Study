package com.zero.study.net.entity

/**
 * @date:2024/8/29 15:11
 * @path:com.zero.study.net.entity.CountryEntity
 *
 * {
 *     "ip": "119.123.134.21",
 *     "city": "Shenzhen",
 *     "region": "Guangdong",
 *     "country": "CN",
 *     "loc": "22.5455,114.0683",
 *     "org": "AS4134 CHINANET-BACKBONE",
 *     "postal": "518000",
 *     "timezone": "Asia/Shanghai",
 *     "readme": "https://ipinfo.io/missingauth"
 * }
 */
data class CountryEntity(val ip: String, val city: String, val region: String, val location: String, val org: String, val timeZone: String, val readme: String)