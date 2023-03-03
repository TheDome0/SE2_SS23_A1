package com.example.se2_ss23_a1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se2_ss23_a1.ui.theme.SE2_SS23_A1Theme
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.channels.UnresolvedAddressException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SE2_SS23_A1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting() {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("11928756") }
    var inputError by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Enter matriculation number")
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = input,
            onValueChange = {
                input = it
                inputError = it.toLongOrNull() == null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = inputError,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                result = send(input)
            }
        }, enabled = !inputError) {
            Text("Send")
        }
        Button(onClick = {
            result = checkPrimes(input)
        }, enabled = !inputError) {
            Text("Check for primes")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(result)

    }
}

suspend fun send(input: String): String {
    return try {
        var response: String? = null
        SelectorManager(Dispatchers.IO).use { selectorManager ->
            val socket = aSocket(selectorManager)
                .tcp()
                .connect("se2-isys.aau.at", 53212) {
                    socketTimeout = 10_000
                }

            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            sendChannel.writeStringUtf8(input + '\n')

            while (response == null) {
                response = receiveChannel.readUTF8Line(limit = 1024)
            }
            socket.close()
        }
        response.toString()
    } catch (e: SocketTimeoutException) {
        "Connection timed out"
    } catch (e: UnresolvedAddressException) {
        "Could not resolve address"
    } catch (e: Exception) {
        "Unknown error"
    }
}

fun checkPrimes(input: String) = input.filter { it in listOf('2', '3', '5', '7') }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SE2_SS23_A1Theme {
        Greeting()
    }
}