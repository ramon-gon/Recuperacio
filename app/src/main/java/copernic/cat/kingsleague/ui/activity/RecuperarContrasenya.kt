package copernic.cat.kingsleague.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import copernic.cat.kingsleague.R
import copernic.cat.kingsleague.databinding.RecuperarContrasenyaBinding


class RecuperarContrasenya : AppCompatActivity() {

    /*Declarem els atributs que inicialitzarem més tard (lateinit) per guardar els components del formulari del activity_login.
      És recomanable per seguretat i facilitar-nos la feina que els noms d'aquests atributs siguin els mateixos que els noms dels
      id dels components del fitxer xml*/

    private lateinit var binding: RecuperarContrasenyaBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        binding = RecuperarContrasenyaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val textView =  binding.titolRecuperarContrasenya
        textView.gravity = Gravity.CENTER

        //Inicialitzem la variable de tipus FirebaseAuth amb una instància d'aquesta classe
        auth = Firebase.auth

        //Implementem els listeners per quan l'usuari cliqui un dels botons

        binding.buttonRecuperarContrasenya.setOnClickListener {
            val correu = binding.textInputEditTextEmail.text.toString().replace(" ", "") //Guardem el correu introduït per l'usuari

            if (correu.isNotEmpty()) {
                resetContrasenya(correu)
            }
        }
        binding.buttonCancelarRecuperarcontrasenya.setOnClickListener {
            //Anem al mainActivity des d'aquesta pantalla
            startActivity(Intent(this, Login::class.java))
            finish() //Alliberem memòria un cop finalitzada aquesta tasca.

        }
    }

    private fun resetContrasenya (email: String) {
        auth.setLanguageCode("ca")
        auth.sendPasswordResetEmail(email).addOnCompleteListener() { task ->
            if (task.isSuccessful)
            {


                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.canviar_contrasenya_alert))
                builder.setPositiveButton(R.string.acceptar, null)
                val dialog = builder.create()
                dialog.show()


            }
            else
            {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.canviar_contrasenya_error))
                builder.setPositiveButton(R.string.acceptar, null)
                val dialog = builder.create()
                dialog.show()

            }
        }
    }

}

