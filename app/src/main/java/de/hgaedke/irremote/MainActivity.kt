package de.hgaedke.irremote

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.hgaedke.irremote.ui.theme.IRRemoteTheme
import de.hgaedke.irremote.websocket.SocketListener
import de.hgaedke.irremote.websocket.WebSocketClient
import androidx.compose.runtime.livedata.observeAsState

class MainActivity : ComponentActivity() {
    private lateinit var webSocketClient: WebSocketClient

    // required, because connectionState is changed from outside the Composable
    private val connectionStateLive = MutableLiveData<ConnectionState>(ConnectionState.CONNECTION_STATE_DISCONNECTED)

    private val socketListener = object : SocketListener {
        override fun onOpen() {
            Log.d("WebSocket", "onOpen")
            connectionStateLive.postValue(ConnectionState.CONNECTION_STATE_CONNECTED)
        }

        override fun onMessage(message: String) {
            Log.d("WebSocket", "onMessage: $message")
        }

        override fun onClosed() {
            Log.d("WebSocket", "onClosed")
            connectionStateLive.postValue(ConnectionState.CONNECTION_STATE_DISCONNECTED)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webSocketClient = WebSocketClient.getInstance()
        webSocketClient.setListener(socketListener)

        //webSocketClient.setSocketUrl("ws://10.0.2.2:8081") // test on emulated Android device, access server on development machine
        webSocketClient.setSocketUrl("ws://192.168.178.39:8081") // run on real device, access Internet Radio in home network

        enableEdgeToEdge()

        setContent {
            IRRemoteTheme {
                MainContent(
                    connect = {
                        webSocketClient.connect()
                    },
                    disconnect = {
                        webSocketClient.disconnect()
                    },
                    sendMessage = {message: String ->
                        webSocketClient.sendMessage(message = message)
                    },
                    connectionStateLive)
            }
        }
    }
}

@Composable
fun MainContent(
    connect: () -> Unit = {},
    disconnect: () -> Unit = {},
    sendMessage: (String) -> Unit = {},
    connectionStateLive: LiveData<ConnectionState> = MutableLiveData<ConnectionState>(ConnectionState.CONNECTION_STATE_DISCONNECTED)
) {
    // ------------------ connection state ------------------
    val connectionState: State<ConnectionState?> = connectionStateLive.observeAsState()

    fun toggleConnectionState() {
        if (connectionState.value == ConnectionState.CONNECTION_STATE_DISCONNECTED) {
            connect()
        } else if (connectionState.value == ConnectionState.CONNECTION_STATE_CONNECTED) {
            disconnect()
        }
    }

    @Composable
    fun CreateConnectionButtons() {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(text = "Connection state:")
            Text(text = connectionState.value?.state.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    toggleConnectionState()
                },
                enabled = connectionState.value == ConnectionState.CONNECTION_STATE_DISCONNECTED
            ) {
                Text(text = "Connect")
            }
            Button(
                onClick = {
                    toggleConnectionState()
                },
                enabled = connectionState.value == ConnectionState.CONNECTION_STATE_CONNECTED
            ) {
                Text(text = "Disconnect")
            }
        }
    }

    // ------------------ text message ------------------
    val textMessage = remember {
        mutableStateOf(TextFieldValue(""))
    }

    Column(modifier = Modifier.padding(10.dp, 60.dp, 10.dp, 10.dp)) {
        CreateConnectionButtons()
        HorizontalDivider(modifier = Modifier.padding(0.dp, 10.dp))
        TextField(value = textMessage.value,
            onValueChange = {
                textMessage.value = it
            },
            label = {
                Text(text = "Enter message")
            },
        )
        Button(onClick = {
                sendMessage(textMessage.value.text)
                textMessage.value = TextFieldValue("")
            },
            content = {
                Text (text = "Send")
            },
            modifier = Modifier.padding(0.dp, 10.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IRRemoteTheme {
        MainContent()
    }
}