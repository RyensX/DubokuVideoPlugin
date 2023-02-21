package com.su.dubokuvideoplugin.plugin.components

import android.util.Log
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.su.dubokuvideoplugin.plugin.Const.host
import com.su.dubokuvideoplugin.plugin.danmaku.OyydsDanmaku
import com.su.dubokuvideoplugin.plugin.danmaku.OyydsDanmakuParser
import com.su.dubokuvideoplugin.plugin.util.JsoupUtil
import com.su.dubokuvideoplugin.plugin.util.Text.trimAll
import com.su.dubokuvideoplugin.plugin.util.oyydsDanmakuApis
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.data.VideoPlayMedia
import com.su.mediabox.pluginapi.util.PluginPreferenceIns

class VideoPlayPageDataComponent : IVideoPlayPageDataComponent {

    companion object {
        private val videoUrlPatten = Regex("url\":\"(.*?)\",\"")
    }

    private var episodeDanmakuId = ""
    override suspend fun getDanmakuData(
        videoName: String,
        episodeName: String,
        episodeUrl: String
    ): List<DanmakuItemData>? {
        try {
            val config = PluginPreferenceIns.get(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, true)
            if (!config)
                return null
            val name = videoName.trimAll()
            var episode = episodeName.trimAll()
            //剧集对集去除所有额外字符，增大弹幕适应性
            val episodeIndex = episode.indexOf("集")
            if (episodeIndex > -1 && episodeIndex != episode.length - 1) {
                episode = episode.substring(0, episodeIndex + 1)
            }
            Log.d("请求Oyyds弹幕", "媒体:$name 剧集:$episode")
            return oyydsDanmakuApis.getDanmakuData(name, episode).data.let { danmukuData ->
                val data = mutableListOf<DanmakuItemData>()
                danmukuData?.data?.forEach { dataX ->
                    OyydsDanmakuParser.convert(dataX)?.also { data.add(it) }
                }
                episodeDanmakuId = danmukuData?.episode?.id ?: ""
                data
            }
        } catch (e: Exception) {
            throw RuntimeException("弹幕加载错误：${e.message}")
        }
    }

    override suspend fun putDanmaku(
        videoName: String,
        episodeName: String,
        episodeUrl: String,
        danmaku: String,
        time: Long,
        color: Int,
        type: Int
    ): Boolean = try {
        Log.d("发送弹幕到Oyyds", "内容:$danmaku 剧集id:$episodeDanmakuId")
        oyydsDanmakuApis.addDanmaku(
            danmaku,
            //Oyyds弹幕标准时间是秒
            (time / 1000F).toString(),
            episodeDanmakuId,
            OyydsDanmakuParser.danmakuTypeMap.entries.find { it.value == type }?.key ?: "scroll",
            String.format("#%02X", color)
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    override suspend fun getVideoPlayMedia(episodeUrl: String): VideoPlayMedia {
        val url = host + episodeUrl
        val document = JsoupUtil.getDocument(url)
        document.getElementsByClass("embed-responsive clearfix").first()?.html()?.also { html ->
            videoUrlPatten.find(html)?.let {
                val epName = document.getElementsByClass("title")
                    .first()?.getElementsByClass("text-muted")?.text() ?: ""
                val videoUrl = it.groups[1]?.value?.replace("\\", "") ?: return@let
                Log.d("解析视频", "集数=$epName 原始链接=$episodeUrl 播放链接=$videoUrl")
                return VideoPlayMedia(epName, videoUrl)
            }
        }
        throw RuntimeException("无法解析出有效播放链接")
    }

}