package vn.io.huangnosimp.utils

data class Version (
    var id: String,
    var type: String,
    var url: String,
    var time: String,
    var releaseTime: String,
    var sha1: String,
    var complianceLevel: Int,
)