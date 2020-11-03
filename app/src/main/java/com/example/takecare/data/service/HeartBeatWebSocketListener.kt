package com.example.takecare.data.service

import android.util.Log
import com.example.takecare.data.repository.DiagnosticRepository
import com.example.takecare.ui.control.ControlViewModel
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class HeartBeatWebSocketListener(val updateUiFromWebSocketData: (String) -> Unit) : WebSocketListener() {
    private val NORMAL_CLOSURE_STATUS = 1000
    private var controlViewModel = ControlViewModel(DiagnosticRepository())

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("TAG", "CONNECTED TO WEBSOCKET!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("RECEIVING SOCKET", text)
        val heartbeat = text.toInt()
        controlViewModel.addDiagnostics(heartbeat)
        updateUiFromWebSocketData(text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
    }
}