package com.example.takecare.ui.control

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.takecare.R
import com.example.takecare.data.repository.DiagnosticRepository
import com.example.takecare.data.service.HeartBeatWebSocketListener
import com.example.takecare.model.Diagnostic
import kotlinx.android.synthetic.main.fragment_control.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.*

class ControlFragment : Fragment() {

    private lateinit var viewModel: ControlViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var dialogButton: Button
    private lateinit var controlContent: TextView
    private lateinit var controlDate: TextView
    private lateinit var controlLevel: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_control, container, false)


        viewModel = ControlViewModel(DiagnosticRepository())
        setupViewModel()

        progressBar = root.control_progressBar
        errorText = root.control_error_text
        dialogButton = root.control_btn_med

        controlContent = root.control_card_content
        controlDate = root.control_card_date
        controlLevel = root.control_card_level

        root.control_btn_med.setOnClickListener {
            val webSocket = startWebSocketConnection()
            root.control_btn_med.isVisible = false
            root.control_progressBar.isVisible = true

            Handler(Looper.getMainLooper()).postDelayed({
                root.control_btn_med.isVisible = true
                root.control_progressBar.isVisible = false
                val closing = 1000
                webSocket.close(closing, null)
            }, 5000)
        }
        return root
    }

    private fun startWebSocketConnection(): WebSocket {
        val request = Request.Builder().url("wss://takecare-websocket.herokuapp.com/").build()
        val listener = HeartBeatWebSocketListener(::updateUiFromWebSocketData)
        val client = OkHttpClient()
        val webSocket = client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()
        return webSocket
    }

    private fun updateUiFromWebSocketData(text: String) {
        controlContent.text = "$text\nLPM"
        val rightNow = Calendar.getInstance()
        val currentHour = rightNow[Calendar.HOUR_OF_DAY]
        val currentMinute = rightNow[Calendar.MINUTE]
        val currentSecond = rightNow[Calendar.SECOND]
        val minutePaddingZero = if (currentMinute < 10) "0" else ""
        val secondPaddingZero = if (currentSecond < 10) "0" else ""
        val time = "Hora: $currentHour:$minutePaddingZero$currentMinute:$secondPaddingZero$currentSecond"
        controlDate.text = time
        val heartbeat = text.toInt()
        controlLevel.text =
            if (heartbeat <= 80) "Sin Ansiedad" else if (heartbeat <= 100) "Bajo" else if (heartbeat <= 120) "Medio" else "Alto"
    }

    private fun setupViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner, isViewLoadingObserver)
        viewModel.isRequestSuccess.observe(viewLifecycleOwner, isRequestSuccess)
        viewModel.onMessageError.observe(viewLifecycleOwner, onMessageError)
        viewModel.diagnosticData.observe(viewLifecycleOwner, diagnosticData)
    }

    private val isViewLoadingObserver = Observer<Boolean> {
        val progressBarVisibility = if (it) View.VISIBLE else View.GONE
        val btnVisibility = if (it) View.INVISIBLE else View.VISIBLE
        progressBar.visibility = progressBarVisibility
        dialogButton.visibility = btnVisibility
    }

    private val isRequestSuccess = Observer<Boolean> {
        if (it) {
            Toast.makeText(this.requireContext(), "Diagnóstico registrado con éxito !", Toast.LENGTH_SHORT).show()
        }
    }

    private val onMessageError = Observer<Any> {
        if(!it.toString().isBlank()){
            progressBar.visibility = View.INVISIBLE
            dialogButton.visibility = View.INVISIBLE
            errorText.visibility = View.VISIBLE
            errorText.text = it.toString()
        }
    }

    private val diagnosticData = Observer<Diagnostic> {
        if(it != null){
            controlContent.text = "${it.frequency.heartRate}\nLPM"
            val date = it.frequency.date.split('/')
            controlDate.text = "Fecha: ${date[2]}-${date[1]}-${date[0]}"
            controlLevel.text = it.level.name
        }
    }
}
