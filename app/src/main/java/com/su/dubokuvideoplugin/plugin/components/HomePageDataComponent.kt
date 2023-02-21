package com.su.dubokuvideoplugin.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.su.dubokuvideoplugin.plugin.Const
import com.su.dubokuvideoplugin.plugin.Const.host
import com.su.dubokuvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo1Data
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    private val layoutSpanCount = 12

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val doc = JsoupUtil.getDocument(url)
        val data = mutableListOf<BaseData>()

        //TODO 这样逻辑更清晰，但没有顺序读取效率高
        doc.getElementsByClass("myui-panel-box clearfix").forEach { videoType ->
            //每个视频分类推荐主元素
            videoType.getElementsByClass("myui-panel__head clearfix").first()?.also { header ->
                //头部信息
                val title = header.getElementsByClass("title").text()
                //更多、链接
                val more =
                    header.getElementsByAttribute("href").run { Pair(text(), this.attr("href")) }
                //具体视频
                val videos = mutableListOf<BaseData>()
                videoType.getElementsByClass("myui-vodlist__thumb lazyload").forEach { video ->
                    val videoName = video.attr("title")
                    val videoImg = video.attr("data-original")
                    val videoUrl = video.attr("href")
                    val videoEpisode =
                        video.getElementsByClass("pic-text text-right").first()?.text() ?: ""
                    videos.add(MediaInfo1Data(videoName, videoImg, videoUrl, videoEpisode)
                        .apply {
                            spanSize = layoutSpanCount / 3
                            action = DetailAction.obtain(videoUrl)
                        })
                    Log.d("视频", "分类:$title 名称:$videoName 链接:$videoUrl 图片:$videoImg")
                }
                if (videos.isNotEmpty()) {
                    data.add(SimpleTextData(title).apply {
                        if (data.isEmpty())
                            layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                        fontSize = 16F
                        fontStyle = Typeface.BOLD
                        fontColor = Color.BLACK
                        spanSize = layoutSpanCount / 2
                    })
                    //TODO 最近更新自定义页面
                    val tmp = more.first.contains("更新")
                    data.add(SimpleTextData("${if (tmp) "" else more.first} >").apply {
                        fontSize = 12F
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        fontColor = Const.INVALID_GREY
                        spanSize = layoutSpanCount / 2
                    }.apply {
                        if (!tmp)
                            action = ClassifyAction.obtain(more.second, "${more.first}$title")
                    })
                    data.addAll(videos)
                    Log.d("添加分类", "名称:$title 数量:${videos.size}")
                }
            }
        }

        return data
    }
}