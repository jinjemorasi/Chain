package dev.chain.vpn.dto

import android.content.Context
import dev.chain.vpn.dto.entities.ProfileItem
import dev.chain.vpn.enums.CoreResolvedType

data class CoreConfigContext(
    val context: Context,
    val guid: String,
    val isCustom: Boolean = false,
    val resolvedOutbounds: List<ResolvedOutbound> = emptyList(),
    val routingDomainRules: List<RoutingDomainRule> = emptyList(),
) {
    data class ResolvedOutbound(
        val tag: String,
        val profile: ProfileItem,
        val resolvedProfiles: List<ProfileItem>,
        val resolvedType: CoreResolvedType,
    )

    data class RoutingDomainRule(
        val domain: List<String>,
        val outboundTag: String,
    )
}
