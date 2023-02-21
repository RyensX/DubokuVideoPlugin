package com.su.dubokuvideoplugin.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.su.dubokuvideoplugin.plugin.Const
import com.su.dubokuvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        var score = -1F
        val upState: String
        val url = Const.host + partUrl
        val document = JsoupUtil.getDocument(url)
        val tags = mutableListOf<TagData>()
        val episodes = mutableListOf<EpisodeData>()

        //头部信息
        document.getElementsByClass("lazyload").first()?.also {
            cover = it.attr("data-original")
        }
        document.getElementsByClass("myui-content__detail").first()?.also { detail ->
            title = detail.getElementsByClass("title").first()?.text() ?: ""
            //评分
            detail.getElementsByClass("branch").first()?.also {
                score = it.text().toFloat()
            }
            //tags
            detail.getElementsByClass("data").first()?.apply {
                //val types=getElementsByTag("span")
                getElementsByTag("a").forEachIndexed { _, element ->
                    tags.add(TagData(element.text()).apply {
                        action = ClassifyAction.obtain(
                            element.attr("href"),
                            "",
                            element.text()
                        )
                    })
                }
            }
        }
        document.select("#desc > div > div.myui-panel_bd > div > span.data > p").first()?.also {
            desc = it.text()
        }
        //剧集
        document.getElementsByClass("myui-content__list scrollbar sort-list clearfix")
            .first()
            ?.children()
            ?.forEach {
                it.getElementsByTag("a").also { ep ->
                    val epUrl = ep.attr("href")
                    episodes.add(EpisodeData(ep.text(), epUrl).apply {
                        action = PlayAction.obtain(epUrl)
                    })
                }
            }
        //更新情况
        upState = "更新至：${episodes.last().name}"

        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover, score = score).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(
                LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(EpisodeListData(episodes))
        })
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}