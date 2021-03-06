package com.example.takecare.ui.login

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.takecare.MainActivity
import com.example.takecare.R
import com.example.takecare.data.repository.UserRepository
import kotlinx.android.synthetic.main.activity_register.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = RegisterViewModel(UserRepository())
        setupViewModel()

        register_birthday.setOnClickListener {
            pickDate(register_birthday)
        }

        register_btn.setOnClickListener {
            val name = register_name.text.trim().toString()
            val lastName = register_lastname.text.trim().toString()
            val mail = register_email.text.trim().toString()
            val username = register_username.text.trim().toString()
            val birthday = register_birthday.text.trim().toString()
            val password = register_password.text.trim().toString()
            val confirmPassword = register_confirm_password.text.trim().toString()
            val terms = register_terms_combo.isChecked

            val patternLength = Pattern.compile("^(?=\\S+$).{6,}$")
            val patternCapital = Pattern.compile( "^(?=.*[A-Z])(?=\\S+$).{6,}$")
            val patternNumber = Pattern.compile("^(?=.*[0-9])(?=\\S+$).{6,}$")

            if(name.isBlank() || lastName.isBlank() || mail.isBlank() || birthday.isBlank() ||
                password.isBlank() || confirmPassword.isBlank() || username.isBlank()){
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }else if(!mail.contains("@") || !mail.contains(".com")){
                Toast.makeText(this, "Correo inválido, ingrese otro correo", Toast.LENGTH_SHORT).show()
            }
            else if (!patternLength.matcher(password).matches()){
                Toast.makeText(this, "Las contraseña debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
            }else if (!patternCapital.matcher(password).matches()){
                Toast.makeText(this, "Las contraseña debe contener al menos una mayúscula", Toast.LENGTH_SHORT).show()
            }else if (!patternNumber.matcher(password).matches()){
                Toast.makeText(this, "Las contraseña debe contener al menos un número", Toast.LENGTH_SHORT).show()
            }else if(password != confirmPassword){
                Toast.makeText(this, "Las contraseñas ingresadas no son iguales", Toast.LENGTH_SHORT).show()
            }else if(!terms){
                Toast.makeText(this, "Debes aceptar los terminos", Toast.LENGTH_SHORT).show()
            }else{
                //Format date for database format
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val formattedDate = LocalDate.parse(birthday, formatter)!!
                val birthdayText = "${formattedDate.year}-${formattedDate.monthValue}-${formattedDate.dayOfMonth}"

                viewModel.register(name, lastName, mail, birthdayText, password, username)
            }
        }
    }

    private fun pickDate(view: EditText){

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val picker = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener{_, mYear, mMonth, mDay  ->
                val monthText = if(mMonth + 1 < 10) "0${mMonth.plus(1)}" else "${mMonth.plus(1)}"
                val dayText = if(mDay < 10) "0${mDay}" else mDay.toString()
                view.setText("" + dayText + "-" + monthText + "-" + mYear)
            }, year, month, day)

        picker.datePicker.maxDate = Date().time
        picker.show()
    }

    private fun setupViewModel() {
        viewModel.isLoading.observe(this, isViewLoadingObserver)
        viewModel.isRequestSuccess.observe(this, isRequestSuccess)
        viewModel.onMessageError.observe(this, onMessageError)
    }

    private val isViewLoadingObserver = Observer<Boolean> {
        val progressBarVisibility = if (it) View.VISIBLE else View.GONE
        val btnVisibility = if (it) View.INVISIBLE else View.VISIBLE
        register_progressBar.visibility = progressBarVisibility
        register_btn.visibility = btnVisibility
    }

    private val isRequestSuccess = Observer<Boolean> {
        if (it) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private val onMessageError = Observer<Any> {
        register_error_text.text = it.toString()
    }
}
