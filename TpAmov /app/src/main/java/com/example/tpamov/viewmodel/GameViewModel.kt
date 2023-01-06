package com.example.tpamov.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tpamov.data.Data
import com.example.tpamov.game.Game
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class GameViewModel : ViewModel() {
    val game = Game()

    companion object {
        const val SERVER_PORT = 9999

        const val MOVE_NONE = 0
        const val MOVE_ROCK = 1
        const val MOVE_PAPER = 2
        const val MOVE_SCISSORS = 3

        const val ME = 1
        const val OTHER = 2
        const val NONE = 0
    }
    enum class State {
        //STARTING, PLAYING_BOTH, PLAYING_ME, PLAYING_OTHER, ROUND_ENDED, GAME_OVER
        STARTING, GAME_OVER
    }
    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    private val _state = MutableLiveData(State.STARTING)
    val state : LiveData<State>
        get() = _state

    private val _connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    val connectionState : LiveData<ConnectionState>
        get() = _connectionState

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null

    private var threadComm: Thread? = null

    fun startClient(serverIP: String,serverPort: Int = SERVER_PORT) {
        if (socket != null || _connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return

        thread {
            _connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {
                //val newsocket = Socket(serverIP, serverPort)
                val newsocket = Socket()
                newsocket.connect(InetSocketAddress(serverIP,serverPort),5000)
                startComm(newsocket)
            } catch (_: Exception) {
                Log.d(TAG,"ERRO DE CONEXÃO DO CLIENTE")
                _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                stopGame()
            }
        }
    }

    fun startServer() {
        if (serverSocket != null || socket != null ||
            _connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return

        _connectionState.postValue(ConnectionState.SERVER_CONNECTING)

        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.run {
                try {
                    val socketClient = serverSocket!!.accept()
                    startComm(socketClient)
                } catch (_: Exception) {
                    Log.d(TAG,"ERRO DE CONEXÃO DO HOST")
                    _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                } finally {
                    serverSocket?.close()
                    serverSocket = null
                }
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        _connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
    }

    fun sendJson() {
        if (_connectionState.value != ConnectionState.CONNECTION_ESTABLISHED)
            return
        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    val msg = "TESTE"
                    printStream.println(msg)
                    printStream.flush()
                } catch (_: Exception) {
                    stopGame()
                }
            }
        }
        //_state.postValue(State.PLAYING_OTHER)
    }

    private fun startComm(newSocket: Socket) {
        if (threadComm != null)
            return

        socket = newSocket

        threadComm = thread {
            try {
                if (socketI == null)
                    return@thread

                _connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                val bufI = socketI!!.bufferedReader()
                Log.d(TAG,"LEU DO SOCKET")

                while (_state.value != State.GAME_OVER) {
                    //SE O JOGO ESTIVER A DECORRER
                    val message = bufI.readLine() //UMA STRING EM JSON
                    val gson = Gson()


                    Log.d(TAG,"MENSAGEM DO SOCKET [$message]")
                    val dataJson = message.toString()
                    sendJson()

                    val dataRecieved = gson.fromJson(dataJson, Data::class.java)
                    println(dataRecieved)
                    Log.d(TAG,"SERVER MSG: $dataRecieved")
                }
            } catch (_: Exception) {
            } finally {
                stopGame()
            }
        }
    }

    fun stopGame() {
        try {
            _state.postValue(State.GAME_OVER)
            _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            threadComm?.interrupt()
            threadComm = null
        } catch (_: Exception) { }
    }


}