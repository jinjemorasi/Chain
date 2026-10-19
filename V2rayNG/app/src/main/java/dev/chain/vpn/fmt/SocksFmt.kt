package dev.chain.vpn.fmt

import dev.chain.vpn.dto.entities.ProfileItem
import dev.chain.vpn.enums.EConfigType
import dev.chain.vpn.extension.idnHost
import dev.chain.vpn.extension.isNotNullEmpty
import dev.chain.vpn.util.Utils
import java.net.URI

object SocksFmt : FmtBase() {
    /**
     * Parses a Socks URI string into a ProfileItem object.
     *
     * @param str the Socks URI string to parse
     * @return the parsed ProfileItem object, or null if parsing fails
     */
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.SOCKS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null

        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        if (uri.userInfo?.isEmpty() == false) {
            val result = if (uri.userInfo.contains(":")) {
                uri.userInfo.split(":", limit = 2)
            } else {
                Utils.decode(uri.userInfo).split(":", limit = 2)
            }
            if (result.count() == 2) {
                config.username = result.first()
                config.password = result.last()
            }
        }

        return config
    }

    /**
     * Converts a ProfileItem object to a URI string.
     *
     * @param config the ProfileItem object to convert
     * @return the converted URI string
     */
    fun toUri(config: ProfileItem): String {
        val pw =
            if (config.username.isNotNullEmpty())
                "${config.username}:${config.password}"
            else
                ":"

        return toUri(config, Utils.encode(pw, true), null)
    }
}