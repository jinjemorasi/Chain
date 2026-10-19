package dev.chain.vpn.handler

import dev.chain.vpn.AppConfig
import dev.chain.vpn.dto.UrlContentRequest
import dev.chain.vpn.dto.entities.SubscriptionCache
import dev.chain.vpn.dto.entities.SubscriptionItem
import dev.chain.vpn.util.HttpUtil
import dev.chain.vpn.util.LogUtil

/**
 * Chain's one-tap "free servers" source.
 *
 * The subscription is stored as a regular [SubscriptionItem] with the stable id
 * [SUB_ID], so all of v2rayNG's existing subscription machinery (replace-on-update,
 * per-server latency test, group display) applies unchanged. Refreshing downloads
 * the plain-text list at [FREE_URL] with caching disabled, then imports it with
 * [AngConfigManager.importBatchConfig] in replace mode (append=false), which
 * deletes only the servers previously imported under [SUB_ID] and adds the fresh
 * ones. The user's own servers and other subscriptions are never touched.
 *
 * All functions that hit the network must be called off the main thread.
 */
object ChainFreeServers {

    /** Stable subscription id used to tag servers imported from the free list. */
    const val SUB_ID = "chain-free"

    /** Plain-text list: one proxy URI per line, '#' lines are comments. */
    const val FREE_URL = "https://raw.githubusercontent.com/patterniha/Free-Configs/main/configs.txt"

    fun ensureSubscription(): SubscriptionCache {
        val existing = MmkvManager.decodeSubscription(SUB_ID)
        if (existing != null) {
            if (existing.url != FREE_URL) {
                existing.url = FREE_URL
                MmkvManager.encodeSubscription(SUB_ID, existing)
            }
            return SubscriptionCache(SUB_ID, existing)
        }
        val item = SubscriptionItem(
            remarks = "سرورهای رایگان",
            url = FREE_URL,
            enabled = true,
            autoUpdate = false,
            requestHeaders = "{\"Cache-Control\": \"no-cache\"}",
        )
        MmkvManager.encodeSubscription(SUB_ID, item)
        LogUtil.i(AppConfig.TAG, "ChainFreeServers: created '$SUB_ID' subscription")
        return SubscriptionCache(SUB_ID, item)
    }

    fun lastUpdatedMillis(): Long =
        MmkvManager.decodeSubscription(SUB_ID)?.lastUpdated ?: -1L

    fun serverCount(): Int =
        MmkvManager.decodeServerList(SUB_ID).size

    /**
     * Downloads the free list (no caching) and replaces the previously imported
     * servers. Returns [RefreshResult.Failure] with a precise kind for
     * download, empty-body, and parse failures.
     */
    fun refresh(): RefreshResult {
        val sub = ensureSubscription()

        val body = try {
            HttpUtil.getUrlContentWithUserAgent(
                UrlContentRequest(
                    url = FREE_URL,
                    timeout = 25000,
                    requestHeaders = "{\"Cache-Control\": \"no-cache\"}",
                )
            )
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "ChainFreeServers: download failed", e)
            return RefreshResult.Failure(FailureKind.NETWORK)
        }
        if (body.isBlank()) {
            LogUtil.w(AppConfig.TAG, "ChainFreeServers: empty response body")
            return RefreshResult.Failure(FailureKind.EMPTY)
        }

        val (count, _) = try {
            // append=false -> commitProfiles replaces only this subscription's servers
            AngConfigManager.importBatchConfig(body, SUB_ID, false)
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "ChainFreeServers: import failed", e)
            return RefreshResult.Failure(FailureKind.PARSE)
        }
        if (count <= 0) {
            LogUtil.w(AppConfig.TAG, "ChainFreeServers: no parsable server lines")
            return RefreshResult.Failure(FailureKind.PARSE)
        }

        sub.subscription.lastUpdated = System.currentTimeMillis()
        MmkvManager.encodeSubscription(SUB_ID, sub.subscription)
        LogUtil.i(AppConfig.TAG, "ChainFreeServers: refreshed, $count servers")
        return RefreshResult.Success(count)
    }

    enum class FailureKind { NETWORK, EMPTY, PARSE }

    sealed interface RefreshResult {
        data class Success(val count: Int) : RefreshResult
        data class Failure(val kind: FailureKind) : RefreshResult
    }
}
