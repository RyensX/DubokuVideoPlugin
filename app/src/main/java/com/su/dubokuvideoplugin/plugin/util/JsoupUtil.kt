package com.su.dubokuvideoplugin.plugin.util

import com.su.dubokuvideoplugin.plugin.Const
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object JsoupUtil {
    /**
     * 获取没有运行js的html
     */
    fun getDocument(url: String): Document =
        run { getHtmlCode(url) }

    private fun getHtmlCode(url: String) = Jsoup.connect(url)
        .userAgent(Const.ua)
        .get()

}