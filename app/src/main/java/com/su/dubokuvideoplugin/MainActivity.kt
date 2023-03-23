package com.su.dubokuvideoplugin

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.su.dubokuvideoplugin.R

class MainActivity : Activity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "请打开媒体盒子APP启动插件123123[${getString(R.string.app_name)}]"
        })
    }
}
