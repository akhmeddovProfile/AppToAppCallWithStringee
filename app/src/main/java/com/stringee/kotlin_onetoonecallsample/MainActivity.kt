package com.stringee.kotlin_onetoonecallsample

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.stringee.StringeeClient
import com.stringee.call.StringeeCall
import com.stringee.call.StringeeCall2
import com.stringee.exception.StringeeError
import com.stringee.kotlin_onetoonecallsample.R.id.*
import com.stringee.kotlin_onetoonecallsample.R.string
import com.stringee.kotlin_onetoonecallsample.databinding.ActivityMainBinding
import com.stringee.listener.StatusListener
import com.stringee.listener.StringeeConnectionListener
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConnect.setOnClickListener(this)
        binding.btnDisconnect.setOnClickListener(this)
        binding.btnVideoCall.setOnClickListener(this)
        binding.btnVideoCall2.setOnClickListener(this)
        binding.btnVoiceCall.setOnClickListener(this)
        binding.btnVoiceCall2.setOnClickListener(this)

        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(true)

        // register data call back
        launcher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_CANCELED) if (result.data != null) {
                if (result.data!!.action != null && result.data!!
                        .action == "open_app_setting"
                ) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(string.app_name)
                    builder.setMessage("Permissions must be granted for the call")
                    builder.setPositiveButton(
                        "Ok"
                    ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
                    builder.setNegativeButton(
                        "Settings"
                    ) { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.cancel()
                        // open app setting
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    builder.create().show()
                }
            }
        }

        initStringee()
    }

    private fun initStringee() {
        Common.client = StringeeClient(this)
        Common.client.setConnectionListener(object : StringeeConnectionListener {
            override fun onConnectionConnected(
                stringeeClient: StringeeClient,
                isReconnecting: Boolean
            ) {
                runOnUiThread {
                    Log.d(Common.TAG, "onConnectionConnected")
                    progressDialog!!.dismiss()
                    binding.tvUserid.text = "Connected as: ${stringeeClient.userId}"
                    Common.reportMessage(this@MainActivity, "StringeeClient is connected.")
                    binding.vConnect.visibility = View.GONE
                    binding.vCall.visibility = View.VISIBLE
                }
            }

            override fun onConnectionDisconnected(
                stringeeClient: StringeeClient,
                isReconnecting: Boolean
            ) {
                runOnUiThread {
                    Log.d(Common.TAG, "onConnectionDisconnected")
                    progressDialog!!.dismiss()
                    binding.tvUserid.text = "Disconnected"
                    Common.reportMessage(this@MainActivity, "StringeeClient is disconnected.")
                }
            }

            override fun onIncomingCall(stringeeCall: StringeeCall) {
                runOnUiThread {
                    Log.d(Common.TAG, "onIncomingCall: callId - ${stringeeCall.callId}")
                    if (Common.isInCall) stringeeCall.reject(object : StatusListener() {
                        override fun onSuccess() {
                        }
                    }) else {
                        Common.callsMap[stringeeCall.callId] = stringeeCall
                        val intent = Intent(
                            this@MainActivity,
                            IncomingCallActivity::class.java
                        ).apply { putExtra("call_id", stringeeCall.callId) }
                        startActivity(intent)
                    }
                }
            }

            override fun onIncomingCall2(stringeeCall2: StringeeCall2) {
                runOnUiThread {
                    Log.d(Common.TAG, "onIncomingCall2: callId - ${stringeeCall2.callId}")
                    if (Common.isInCall) stringeeCall2.reject(object : StatusListener() {
                        override fun onSuccess() {
                        }
                    }) else {
                        Common.call2sMap[stringeeCall2.callId] = stringeeCall2
                        val intent = Intent(
                            this@MainActivity,
                            IncomingCall2Activity::class.java
                        ).apply { putExtra("call_id", stringeeCall2.callId) }
                        startActivity(intent)
                    }
                }
            }

            override fun onConnectionError(
                stringeeClient: StringeeClient,
                stringeeError: StringeeError
            ) {
                runOnUiThread {
                    Log.d(Common.TAG, "onConnectionError: ${stringeeError.message}")
                    progressDialog!!.dismiss()
                    Common.reportMessage(
                        this@MainActivity,
                        "StringeeClient fails to connect: ${stringeeError.message}"
                    )
                }
            }

            override fun onRequestNewToken(stringeeClient: StringeeClient) {
                runOnUiThread {
                    Log.d(
                        Common.TAG,
                        "onRequestNewToken"
                    )
                    // gen new access token and connect
                    val userId =
                        PrefUtils.getInstance(this@MainActivity)?.getString(Common.PREF_USER_ID, "")
                    if (!(userId == null || userId.isEmpty())) {
                        val token = genAccessToken(userId)
                        PrefUtils.getInstance(this@MainActivity)
                            ?.putString(Common.PREF_ACCESS_TOKEN, token)
                        Common.client.connect(token)
                    } else {
                        Common.client.disconnect()
                        PrefUtils.clearData()
                        binding.vConnect.visibility = View.VISIBLE
                        binding.vCall.visibility = View.GONE
                    }
                }
                // Get new token here and connect to Stringe server
            }

            override fun onCustomMessage(from: String, msg: JSONObject) {
                runOnUiThread {
                    Log.d(
                        Common.TAG,
                        "onCustomMessage: from - $from - msg - $msg"
                    )
                }
            }

            override fun onTopicMessage(from: String, msg: JSONObject) {
            }
        })
        val token = PrefUtils.getInstance(this)?.getString(Common.PREF_ACCESS_TOKEN, "")
        if (!(token == null || token.trim().isEmpty())) {
            Common.client.connect(token)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            btn_video_call -> makeCall(isStringeeCall = true, isVideoCall = true)
            btn_video_call2 -> makeCall(isStringeeCall = false, isVideoCall = true)
            btn_voice_call -> makeCall(isStringeeCall = true, isVideoCall = false)
            btn_voice_call2 -> makeCall(isStringeeCall = false, isVideoCall = false)
            btn_connect -> {
                val userId = binding.etUserId.text.toString()
                if (userId.trim().isNotEmpty()) {
                    progressDialog?.show()
                    val token = genAccessToken(userId)
                    PrefUtils.getInstance(this)?.putString(Common.PREF_ACCESS_TOKEN, token)
                    PrefUtils.getInstance(this)?.putString(Common.PREF_USER_ID, userId)
                    Common.client.connect(token)
                    binding.etUserId.setText("")
                }
            }
            btn_disconnect -> {
                Common.client.disconnect()
                PrefUtils.clearData()
                binding.vConnect.visibility = View.VISIBLE
                binding.vCall.visibility = View.GONE
            }
        }
    }

    private fun makeCall(isStringeeCall: Boolean, isVideoCall: Boolean) {
        val to: String = binding.etTo.text.toString().trim()
        if (to.isNotBlank()) {
            if (Common.client.isConnected) {
                val intent: Intent = if (isStringeeCall)
                    Intent(
                        this@MainActivity,
                        OutgoingCallActivity::class.java
                    ) else Intent(
                    this@MainActivity,
                    OutgoingCall2Activity::class.java
                )
                intent.putExtra("from", Common.client.userId)
                intent.putExtra("to", to)   //to
                intent.putExtra("is_voice_call", isStringeeCall)
                intent.putExtra("is_video_call", isVideoCall)
                launcher.launch(intent)
            } else {
                Common.reportMessage(this, "Stringee session not connected")
            }
        }
    }

    private fun genAccessToken(userId: String): String? {
        val keySid = "SK.0.ihhKfVfPa4NvmmQa5ABjR7hz4UVDKvDv"
        val keySecret = "QlRqSGs5S1hGVXRudHd5UjNXc3pKbTJ3WGYzaUZEaA=="
        val expireInSecond = 1000

        try {
            val algorithmHS: Algorithm = Algorithm.HMAC256(keySecret)

            val headerClaims: MutableMap<String, Any> =
                HashMap()
            headerClaims["typ"] = "JWT"
            headerClaims["alg"] = "HS256"
            headerClaims["cty"] = "stringee-api;v=1"
            val exp = System.currentTimeMillis() + expireInSecond * 1000
            return JWT.create().withHeader(headerClaims)
                .withClaim("jti", keySid + "-" + System.currentTimeMillis())
                .withClaim("iss", keySid)
                .withClaim("rest_api", true)
                .withClaim("userId", userId)
                .withExpiresAt(Date(exp))
                .sign(algorithmHS)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

}