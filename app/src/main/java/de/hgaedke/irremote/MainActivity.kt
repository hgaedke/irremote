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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private lateinit var webSocketClient: WebSocketClient

    // required, because connectionState is changed from outside the Composable
    private val connectionStateLive = MutableLiveData<ConnectionState>(ConnectionState.CONNECTION_STATE_DISCONNECTED)
    private val irStatusLive = MutableLiveData<IRStatus>()

    private val socketListener = object : SocketListener {
        override fun onOpen() {
            Log.d("WebSocket", "onOpen")
            connectionStateLive.postValue(ConnectionState.CONNECTION_STATE_CONNECTED)

            // initially, request the internet radio status
            webSocketClient.requestStatus()
        }

        override fun onMessage(message: String) {
            Log.d("WebSocket", "onMessage: $message")
            try {
                val irStatus: IRStatus = Json.decodeFromString<IRStatus>(message)
                Log.d("WebSocket", "irStatus: $irStatus")
                irStatusLive.postValue(irStatus)
            } catch (e: SerializationException) {
                Log.i("WebSocket", "onMessage: $message is not a valid IRStatus JSON message.\n$e")
            }
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

        webSocketClient.connect()

        enableEdgeToEdge()

        setContent {
            IRRemoteTheme {
                MainContent(
                    sendMessage = {message: String ->
                        webSocketClient.sendNotification(message = message)
                    },
                    requestStatus = {
                        webSocketClient.requestStatus()
                    },
                    selectApp = {app: String ->
                        webSocketClient.selectApp(app)
                    },
                    connectionStateLive,
                    irStatusLive)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        webSocketClient.disconnect()
    }
}

@Composable
fun MainContent(
    sendMessage: (String) -> Unit = {},
    requestStatus: () -> Unit = {},
    selectApp: (String) -> Unit = {},
    connectionStateLive: LiveData<ConnectionState> = MutableLiveData<ConnectionState>(ConnectionState.CONNECTION_STATE_DISCONNECTED),
    irStatusLive: LiveData<IRStatus> = MutableLiveData<IRStatus>()
) {
    val connectionState: State<ConnectionState?> = connectionStateLive.observeAsState()
    val irStatus: State<IRStatus?> = irStatusLive.observeAsState()

    // ------------------ connection state ------------------
    @Composable
    fun CreateConnectionStateInfo() {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(text = "Connection state:")
            Text(text = connectionState.value?.state.toString())
        }
    }

    // ------------------ text message ------------------
    val textMessage = remember {
        mutableStateOf(TextFieldValue(""))
    }

    Column(modifier = Modifier.padding(10.dp, 60.dp, 10.dp, 10.dp)) {
        CreateConnectionStateInfo()
        HorizontalDivider(modifier = Modifier.padding(0.dp, 10.dp))
        TextField(
            value = textMessage.value,
            onValueChange = {
                textMessage.value = it
            },
            label = {
                Text(text = "Enter message")
            },
            enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
        )
        Button(
            onClick = {
                sendMessage(textMessage.value.text)
                textMessage.value = TextFieldValue("")
            },
            content = {
                Text(text = "Send")
            },
            modifier = Modifier.padding(0.dp, 10.dp),
            enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
        )
        HorizontalDivider(modifier = Modifier.padding(0.dp, 10.dp))
        TextField(
            value = irStatus.value.toString(),
            onValueChange = {},
            label = {
                Text(text = "IRStatus")
            },
            enabled = false
        )
        Button(
            onClick = {
                requestStatus()
            },
            content = {
                Text(text = "Update status")
            },
            modifier = Modifier.padding(0.dp, 10.dp),
            enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
        )
        HorizontalDivider(modifier = Modifier.padding(0.dp, 10.dp))
        Text(text = "Select app:")
        Row {
            Button(
                onClick = {
                    selectApp("radio1")
                },
                content = {
                    Text(text = "radio1")
                },
                modifier = Modifier.padding(0.dp, 10.dp),
                enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
            )
            Button(
                onClick = {
                    selectApp("radio2")
                },
                content = {
                    Text(text = "radio2")
                },
                modifier = Modifier.padding(0.dp, 10.dp),
                enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
            )
            Button(
                onClick = {
                    selectApp("music")
                },
                content = {
                    Text(text = "music")
                },
                modifier = Modifier.padding(0.dp, 10.dp),
                enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
            )
            Button(
                onClick = {
                    selectApp("video")
                },
                content = {
                    Text(text = "video")
                },
                modifier = Modifier.padding(0.dp, 10.dp),
                enabled = (connectionState.value === ConnectionState.CONNECTION_STATE_CONNECTED)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IRRemoteTheme {
        MainContent()
    }
}