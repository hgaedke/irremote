package de.hgaedke.irremote.websocket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient {
    private lateinit var webSocket: WebSocket
    private var socketListener: SocketListener? = null
    private var socketUrl = "" // set via setSocketUrl
    private var enableReconnect = true
    private var client: OkHttpClient? = null

    // singleton instance of WebSocket
    companion object {
        private lateinit var instance: WebSocketClient
        @JvmStatic
        @Synchronized
        fun getInstance(): WebSocketClient {
            synchronized(WebSocketClient::class) {
                if (!::instance.isInitialized) {
                    instance = WebSocketClient()
                }
            }
            return instance
        }
    }

    fun setListener(listener: SocketListener) {
        this.socketListener = listener
    }

    fun setSocketUrl(socketUrl: String) {
        this.socketUrl = socketUrl
    }

    private fun initWebSocket() {
        Log.e("socketCheck", "initWebSocket() socketurl = $socketUrl")
        client = OkHttpClient()
        val request = Request.Builder().url(url = socketUrl).build()
        webSocket = client!!.newWebSocket(request, localWebSocketListener)

        // this must me done to prevent memory leak
        client!!.dispatcher.executorService.shutdown()
    }

    fun connect() {
        Log.d("WebSocket", "connect()")
        enableReconnect = true
        initWebSocket()
    }

    fun reconnect() {
        Log.d("WebSocket", "reconnect()")
        initWebSocket()
    }

    fun sendNotification(message: String) {
        val jsonMessage = "{\"notification\": \"$message\"}"
        Log.d("WebSocket", "sendMessage($jsonMessage)")
        if (::webSocket.isInitialized) {
            webSocket.send(jsonMessage)
        }
    }

    fun selectApp(app: String) {
        val jsonMessage = "{\"app\": \"$app\"}"
        Log.d("WebSocket", "selectApp($jsonMessage)")
        if (::webSocket.isInitialized) {
            webSocket.send(jsonMessage)
        }
    }

    fun disconnect() {
        Log.d("WebSocket", "disconnect()")
        if (::webSocket.isInitialized) {
            webSocket.close(1000 /*normal closure*/, null /*reason*/)
        }
        enableReconnect = false
    }

    fun requestStatus() {
        Log.d("WebSocket", "requestStatus()")
        if (::webSocket.isInitialized) {
            webSocket.send("{\"status\": \"get\"}")
        }
    }

    // Handles reconnection locally; forwards certain calls to socketListener.
    private val localWebSocketListener = object : WebSocketListener() {
        // called when connection established successfully
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "onOpen()")
            socketListener?.onOpen()
        }

        // called when message received from internet radio
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "onMessage($text)")
            socketListener?.onMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "onClosing()")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "onClosed()")
            socketListener?.onClosed()

            if (enableReconnect) {
                reconnect()
            }
        }

        override fun onFailure(
            webSocket: WebSocket, t: Throwable, response: Response?
        ) {
            Log.d("WebSocket", "onFailure(): " + t.message)
            socketListener?.onClosed()

            if (enableReconnect) {
                reconnect()
            }
        }
    }
}
