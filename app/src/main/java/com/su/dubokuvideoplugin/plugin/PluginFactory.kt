package com.su.dubokuvideoplugin.plugin

import com.su.mediabox.pluginapi.components.*
import com.su.mediabox.pluginapi.IPluginFactory
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.dubokuvideoplugin.plugin.components.*
import com.su.dubokuvideoplugin.plugin.danmaku.OyydsDanmaku

/**
 * 每个插件必须实现本类
 *
 * 注意包和类名都要相同，且必须提供公开的无参数构造方法
 */
class PluginFactory : IPluginFactory() {

    override val host: String = Const.host

    override fun pluginLaunch() {
        PluginPreferenceIns.apply {
            initKey(Const.HOST_KEY, defaultValue = Const.HOST_DEFAULT)
            initKey(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, defaultValue = true)
        }
    }

    override fun <T : IBasePageDataComponent> createComponent(clazz: Class<T>) = when (clazz) {
        IHomePageDataComponent::class.java -> HomePageDataComponent()
        IVideoPlayPageDataComponent::class.java -> VideoPlayPageDataComponent()
        IMediaSearchPageDataComponent::class.java -> MediaSearchPageDataComponent()
        IMediaDetailPageDataComponent::class.java -> MediaDetailPageDataComponent()
        IMediaClassifyPageDataComponent::class.java -> MediaClassifyPageDataComponent()
        IMediaUpdateDataComponent::class.java -> MediaUpdateDataComponent
        else -> null
    } as? T

}