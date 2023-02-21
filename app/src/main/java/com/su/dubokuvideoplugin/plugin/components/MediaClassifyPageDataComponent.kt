package com.su.dubokuvideoplugin.plugin.components

import android.util.Log
import com.su.dubokuvideoplugin.plugin.Const
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.dubokuvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.MediaInfo1Data
import com.su.mediabox.pluginapi.util.UIUtil.dp

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        //https://u.duboku.io/vodshow/2-----------.html
        val doc = JsoupUtil.getDocument("${Const.host}vodshow/2-----------.html")
        doc.getElementsByClass("slideDown-box").first()?.children()?.forEach { category ->
            //每个分类组主元素
            val types = category.children()
            val categoryName = types[0].text()
            types.forEachIndexed { index, classify ->
                if (index > 1) {
                    classifyItemDataList.add(ClassifyItemData().apply {
                        action = ClassifyAction.obtain(
                            classify.children().first()?.attr("HREF"),
                            categoryName,
                            classify.text()
                        )
                    })
                }
            }
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        //https://u.duboku.io/vodshow/13--------1---.html
        //https://u.duboku.io/vodshow/13-----------.html
        val url = Const.host +
                classifyAction.url?.run { substring(1, length) }
                    ?.replace("---.html", "${page}---.html")
        Log.d("获取分类数据", url)
        val document = JsoupUtil.getDocument(url)
        val layoutSpanCount = 12
        document.getElementsByClass("myui-vodlist__thumb lazyload")?.forEach { video ->
            val videoName = video.attr("title")
            val videoImg = video.attr("data-original")
            val videoUrl = video.attr("href")
            val videoEpisode =
                video.getElementsByClass("pic-text text-right").first()?.text() ?: ""
            classifyList.add(
                MediaInfo1Data(videoName, videoImg, videoUrl, videoEpisode)
                    .apply {
                        spanSize = layoutSpanCount / 3
                        action = DetailAction.obtain(videoUrl)
                    })
            Log.d("视频", "名称:$videoName 链接:$videoUrl 图片:$videoImg")
        }
        classifyList.first().layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
        return classifyList
    }

}