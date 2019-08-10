package com.safframework.netty4android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.Toast
import com.safframework.netty4android.R
import kotlinx.android.synthetic.main.activity_config_client.*

/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.ConfigClientActivity
 * @author: Tony Shen
 * @date: 2019-08-10 12:01
 * @version: V1.0 <描述当前版本功能>
 */
class ConfigClientActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_client)

        if (intent.extras!=null) {

            val ip = intent.extras.getString("ip")
            ip_edit.text = Editable.Factory.getInstance().newEditable(ip)
            ip_edit.setSelection(ip.length)

            val port = intent.extras.getInt("port")
            port_edit.text = Editable.Factory.getInstance().newEditable(port.toString())
            port_edit.setSelection(port_edit.text.toString().length)
        }

        update.setOnClickListener {

            if (ip_edit.text.isNotBlank() && port_edit.text.isNotBlank()) {

                val intent = Intent(this@ConfigClientActivity, MainActivity::class.java)
                intent.putExtra("port", port_edit.text.toString())
                intent.putExtra("ip", ip_edit.text.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {

                Toast.makeText(this@ConfigClientActivity, "请输入IP、端口号", Toast.LENGTH_LONG).show()
            }
        }
    }
}