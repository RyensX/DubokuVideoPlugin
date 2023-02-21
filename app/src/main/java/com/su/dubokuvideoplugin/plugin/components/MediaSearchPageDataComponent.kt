package com.su.dubokuvideoplugin.plugin.components

import android.util.Log
import com.su.dubokuvideoplugin.plugin.Const
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.dubokuvideoplugin.plugin.Const.host
import com.su.dubokuvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.TagData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        //https://u.duboku.io/vodsearch/测试----------1---.html
        val url = "${Const.host}vodsearch/$keyWord----------${page}---.html"
        val document = JsoupUtil.getDocument(url)

        document.getElementById("searchList")?.children()?.apply {
            Log.d("搜索结果", "数量:$size")
        }?.forEach { result ->
            //每个搜索结果的主元素
            result.getElementsByClass("myui-vodlist__thumb img-lg-150 img-md-150 img-sm-150 img-xs-100 lazyload")
                .first()?.also { leftEm ->
                    val title = leftEm.attr("title")
                    val cover = leftEm.attr("data-original")
                    val videoUrl = leftEm.attr("href")
                    val episode = leftEm.getElementsByClass("pic-text text-right").first()?.run {
                        text()
                    } ?: ""
                    var desc = ""
                    var tags: List<TagData>? = null
                    result.getElementsByClass("detail").first()?.also { rightEm ->
                        desc = rightEm.select("p.hidden-xs").first()?.ownText() ?: desc
                        //TODO tag提取有点麻烦
                    }
                    Log.d("添加搜索结果", "名称:$title 封面:$cover 链接:$videoUrl")
                    searchResultList.add(MediaInfo2Data(
                        title, cover, host + videoUrl,
                        episode, desc, tags
                    )
                        .apply {
                            action = DetailAction.obtain(videoUrl)
                        })
                }

        }
        return searchResultList
    }

}