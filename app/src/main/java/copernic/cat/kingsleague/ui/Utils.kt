package copernic.cat.kingsleague.ui

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern


class Utils {
    private var bd = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    //funcion que devuelve una cadena de texto que representa el correo electrónico del usuario actual.
    fun getCorreoUserActural(): String {
        auth = Firebase.auth
        val currentUser = auth.currentUser
        return currentUser!!.email.toString()
    }




    //validar con regex correo electronico
    fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile(
            "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"
        )
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    //validar con regex numero de telefono
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val pattern = Pattern.compile("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*\$")
        val matcher = pattern.matcher(phoneNumber)
        return matcher.matches()
    }

    }


