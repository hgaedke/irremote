package de.hgaedke.irremote.websocket

interface SocketListener {
    fun onOpen()
    fun onMessage(message: String)
    fun onClosed()
}