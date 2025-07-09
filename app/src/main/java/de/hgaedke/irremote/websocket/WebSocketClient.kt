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

    companion object {
        private lateinit var instance: WebSocketClient
        @JvmStatic
        @Synchronized
        //This function gives singleton instance of WebSocket.
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

        //this must me done else memory leak will be caused
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

    //send
    fun sendMessage(message: String) {
        Log.d("WebSocket", "sendMessage($message)")
        if (::webSocket.isInitialized) {
            webSocket.send(message)
        }
    }


    //We can close socket by two way:

    //1. websocket.webSocket.close(1000, "Dont need connection")
    //This attempts to initiate a graceful shutdown of this web socket.
    //Any already-enqueued messages will be transmitted before the close message is sent but
    //subsequent calls to send will return false and their messages will not be enqueued.

    //2. websocket.cancel()
    //This immediately and violently release resources held by this web socket,
    //discarding any enqueued messages.

    //Both does nothing if the web socket has already been closed or canceled.
    fun disconnect() {
        Log.d("WebSocket", "disconnect()")
        if (::webSocket.isInitialized) {
            webSocket.close(1000 /*normal closure*/, null /*reason*/)
        }
        enableReconnect = false
    }

    // Handles reconnection locally; forwards certain calls to socketListener.
    private val localWebSocketListener = object : WebSocketListener() {
        //called when connection succeeded
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "onOpen()")
            socketListener?.onOpen()
        }

        //called when text message received
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "onMessage($text)")
            socketListener?.onMessage(text)
        }

        //called when binary message received
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "onClosing()")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            //called when no more messages and the connection should be released
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
