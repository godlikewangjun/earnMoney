/*
 * Copyright 2014-2017 Eduard Ereza Martínez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wj.makebai.ui.activity.comm

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.wj.commonlib.utils.CommTools
import com.wj.makebai.BuildConfig
import com.wj.makebai.R
import kotlinx.android.synthetic.main.activity_custom_error.*

class CustomErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_error)

//        val errorDetailsText = findViewById<TextView>(R.id.error_details)
//        if(BuildConfig.DEBUG)errorDetailsText.text = CustomActivityOnCrash.getStackTraceFromIntent(intent) else errorDetailsText.text ="出错啦"
//
//        val restartButton = findViewById<Button>(R.id.restart_button)
//
//        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
//
//        if (config == null) {
//            //This should never happen - Just finish the activity to avoid a recursive crash.
//            finish()
//            return
//        }
//
//        if (config.isShowRestartButton && config.restartActivityClass != null) {
//            restartButton.text = "重启app"
//            restartButton.setOnClickListener {
//                CustomActivityOnCrash.restartApplication(
//                    this@CustomErrorActivity,
//                    config
//                )
//            }
//        } else {
//            restartButton.setOnClickListener {
//                CustomActivityOnCrash.closeApplication(
//                    this@CustomErrorActivity,
//                    config
//                )
//            }
//        }
        copy.setOnClickListener { CommTools.copy(this,error_details) }
    }
}
