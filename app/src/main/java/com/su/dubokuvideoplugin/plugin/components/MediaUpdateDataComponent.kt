package com.su.dubokuvideoplugin.plugin.components

import com.su.dubokuvideoplugin.plugin.Const
import com.su.mediabox.pluginapi.components.IMediaUpdateDataComponent
import com.su.dubokuvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.EpisodeData

object MediaUpdateDataComponent : IMediaUpdateDataComponent {

    override suspend fun getUpdateTag(detailUrl: String): String? {
        val doc = JsoupUtil.getDocument(Const.host + detailUrl)
        return try {
            doc.getElementsByClass("myui-content__list scrollbar sort-list clearfix")
                .first()
                ?.children()
                ?.last()?.text()
        } catch (_: Exception) {
            null
        }
    }

}