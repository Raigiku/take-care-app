package com.example.takecare.ui.advice.treatment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.takecare.R
import com.example.takecare.adapter.TreatmentAdapter
import com.example.takecare.data.repository.TreatmentRepository
import com.example.takecare.model.Treatment
import com.example.takecare.ui.history.HistoryViewModel
import kotlinx.android.synthetic.main.activity_treatment.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

class TreatmentActivity : AppCompatActivity() {

    private lateinit var recyclerTreatmentAdapter : TreatmentAdapter
    private lateinit var recyclerViewTreatment : RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var viewModel: TreatmentViewModel
    private lateinit var treatmentList: ArrayList<Treatment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treatment)

        viewModel = TreatmentViewModel(TreatmentRepository())
        setupViewModel()
        viewModel.getTreatments()

        progressBar = treatment_progressBar
        errorText = treatment_error_text

        //Recycler View setup
        treatmentList = ArrayList<Treatment>()
        recyclerTreatmentAdapter = TreatmentAdapter(treatmentList)
        recyclerViewTreatment = recyclerview_treatment
    }

    private fun setupViewModel() {
        viewModel.isLoading.observe(this, isViewLoadingObserver)
        viewModel.isRequestSuccess.observe(this, isRequestSuccess)
        viewModel.onMessageError.observe(this, onMessageError)
        viewModel.treatmentData.observe(this, treatmentData)
    }

    private val isViewLoadingObserver = Observer<Boolean> {
        val progressBarVisibility = if (it) View.VISIBLE else View.GONE
        val btnVisibility = if (it) View.INVISIBLE else View.VISIBLE
        progressBar.visibility = progressBarVisibility
        recyclerViewTreatment.visibility = btnVisibility
        text_past_treatment.visibility = btnVisibility
        text_active_treatment.visibility = btnVisibility
    }

    private val isRequestSuccess = Observer<Boolean> {
        if (it) {

            var activeTreatment:Treatment? = null

            for (treatment in treatmentList){
                if(treatment.status == 1){
                    activeTreatment = treatment
                    break
                }
            }

            if(activeTreatment != null){
                active_treatment.visibility = View.VISIBLE

                var medicationDescription = ""
                for (medicine in activeTreatment.details){
                    val days = ( medicine.quantity.toDouble() / (24.0/ medicine.frequency.toDouble()) )
                    medicationDescription += "${medicine.quantity} pastillas de ${medicine.name} " +
                            "cada ${medicine.frequency} horas por ${ceil(days).toInt()} días\n"
                }

                active_treatment_card_medication.text = medicationDescription
                active_treatment_card_indications.text = activeTreatment.indications

                val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val treatmentDate = LocalDate.parse(activeTreatment.creationDate, dbFormatter)

                val monthText = if(treatmentDate.monthValue < 10) "0${treatmentDate.monthValue}" else treatmentDate.monthValue.toString()
                val dayText = if(treatmentDate.dayOfMonth < 10) "0${treatmentDate.dayOfMonth}" else treatmentDate.dayOfMonth.toString()

                active_treatment_card_date.text = "Fecha: ${dayText}-${monthText}-${treatmentDate.year}"
                active_treatment_card_psychiatrist.text = "Psiquiatra: ${activeTreatment.psychiatristName} ${activeTreatment.psychiatristLastName}"

                treatmentList.remove(activeTreatment)
            }else{
                active_treatment_error.text = getString(R.string.treatment_no_active)
                active_treatment_error.visibility = View.VISIBLE
            }

            if(treatmentList.size == 0){
                recyclerViewTreatment.visibility = View.INVISIBLE
                errorText.visibility = View.VISIBLE
                errorText.text = getString(R.string.treatment_no_past)
            }else{
                recyclerTreatmentAdapter = TreatmentAdapter(treatmentList)
                recyclerViewTreatment.apply {
                    adapter = recyclerTreatmentAdapter
                    layoutManager = LinearLayoutManager(context)
                }
            }
        }
    }

    private val onMessageError = Observer<Any> {
        if(!it.toString().isBlank()){
            progressBar.visibility = View.INVISIBLE
            active_treatment.visibility = View.INVISIBLE
            recyclerViewTreatment.visibility = View.INVISIBLE

            active_treatment_error.visibility = View.VISIBLE
            errorText.visibility = View.VISIBLE
            active_treatment_error.text = it.toString()
            errorText.text = it.toString()
        }
    }

    private val treatmentData = Observer<List<Treatment>> {
        if(!it.isNullOrEmpty()){
            treatmentList.addAll(ArrayList(it))
        }
    }
}
