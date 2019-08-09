package com.safframework.netty4android.ui

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.safframework.netty4android.R
import kotlinx.android.synthetic.main.activity_config_server.*
import android.content.Intent
import android.widget.Toast


/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.ConfigServerActivity
 * @author: Tony Shen
 * @date: 2019-08-09 12:04
 * @version: V1.0 <描述当前版本功能>
 */
class ConfigServerActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_server)

        update.setOnClickListener {

            if (port_edit.text.isNotBlank()) {

                val intent = Intent(this@ConfigServerActivity, MainActivity::class.java)
                intent.putExtra("port", port_edit.text.toString())
                intent.putExtra("webSocketPath", web_socket_path_edit.text.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {

                Toast.makeText(this@ConfigServerActivity, "请输入端口号", Toast.LENGTH_LONG).show()
            }
        }
    }
}