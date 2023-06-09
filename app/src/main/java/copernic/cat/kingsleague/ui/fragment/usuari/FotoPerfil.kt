package copernic.cat.kingsleague.ui.fragment.usuari

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import copernic.cat.kingsleague.R
import copernic.cat.kingsleague.databinding.FragmentFotoPerfilBinding
import copernic.cat.kingsleague.ui.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class FotoPerfil: Fragment() {
    private var photoSelectedUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private val utils = Utils()
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.getReference().child("image/imatges").child(".png")

    private var _binding: FragmentFotoPerfilBinding? = null
    private val binding get() = _binding!!

    /**
     * Método que se llama cuando se crea la actividad. Se encarga de inicializar la autenticación de Firebase.
     * @param savedInstanceState guarda el estado de la aplicación en caso de cambio de configuración
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auth = Firebase.auth
        }
    }

    /**
     * Crea y devuelve la vista del fragmento de autorizaciones de administrador.
     * @param inflater El inflador de layout utilizado para inflar la vista.
     * @param container El contenedor padre de la vista.
     * @param savedInstanceState El estado previamente guardado del fragmento.
     * @return La vista del fragmento de autoritzacionsUsuari.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFotoPerfilBinding.inflate(inflater)
        return binding.root
    }

    /**
     * La función onViewCreated se ejecuta después de onCreateView, y se utiliza para inicializar elementos de la interfaz de usuario.
     * En este caso, se está utilizando el método launch para iniciar una tarea en segundo plano para cargar la
     * imagen del perfil del usuario si ya existe. Además, se establecen los listeners en los botones imgButtonBuscar
     * y button4, para abrir un selector de imágenes y guardar la imagen seleccionada respectivamente.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //metode per carregar l'imatge si ya esta creada
        lifecycleScope.launch() {
                carregarImatge()
            }

        binding.imgButtonBuscar.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    obrirImatge()
                }
            }
            //metode per afegir l'imatge
        }
        binding.btnGuardar.setOnClickListener() {
            lifecycleScope.launch {
                afegirImatge()
            }
            findNavController().navigate(R.id.action_fotoPerfil_to_menuUsuari)
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(getString(R.string.fotoguardadaalert))
            builder.setPositiveButton("Aceptar", null)
            val dialog = builder.create()
            dialog.show()
        }

        binding.btnCancelarfotoPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_fotoPerfil_to_menuUsuari)
        }
    }

    /**
     * Esta función es un registro para el resultado de la actividad que se encarga de abrir la cámara del dispositivo
     * para poder seleccionar una imagen. Si el resultado de la actividad es exitoso, se asigna el URI de la imagen seleccionada
     * y se llama al método "afegirImatge" para subir la imagen al storage de Firebase.
     */
    private val guardarImgCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = result.data?.data //Assignem l'URI de la imatge
                //metode per afegir l'imatge
                binding.imgFotoDePerfil.setImageURI(photoSelectedUri)
            }
        }


    /**
     * Esta función se encarga de añadir una imagen seleccionada al storage de Firebase, asignando a la imagen un
     * nombre con el correo del usuario actual. Utiliza el método putFile de FirebaseStorage para subir la imagen
     * seleccionada y asignarle el nombre con el correo del usuario actual. Además, se encarga de cargar la imagen
     * en el ImageView correspondiente.
     */
    private suspend fun afegirImatge() {
        lifecycleScope.launch {
            var correo = utils.getCorreoUserActural()
            storageRef = storage.reference.child("image/imatges").child("$correo.png")
            //Afegim la imatge seleccionada a storage
            photoSelectedUri?.let { uri -> //Hem seleccionat una imatge...
                //Afegim (pujem) la imatge que hem seleccionat mitjançant el mètode putFile de la classe FirebasStorage, passant-li com a
                //paràmetre l'URI de la imatge.
                storageRef.putFile(uri).await()
                val storageRef =
                    FirebaseStorage.getInstance().reference.child("image/imatges/$correo.png")
                val localfile = File.createTempFile("tempImage", "png")
                storageRef.getFile(localfile).await()
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                binding.imgFotoDePerfil.setImageBitmap(bitmap)
            }
        }
    }

    /**
     * Método que se encarga de cargar la imagen del perfil del usuario en caso de ya estar creada.
     * Se utiliza el método getFile de la clase FirebaseStorage para obtener la imagen almacenada en el servidor.
     * Se utiliza un archivo temporal para almacenar la imagen obtenida y se convierte en un bitmap para poder mostrarla en la interfaz.
     * En caso de error se maneja dentro del catch.
     */
    suspend fun carregarImatge() {
        val correo = utils.getCorreoUserActural()
        val storageRef = FirebaseStorage.getInstance().reference.child("image/imatges/$correo.png")
        try {// la imagen existe, descargar y mostrar la imagen
             storageRef.downloadUrl.await()
            val localfile = withContext(Dispatchers.IO) {
                File.createTempFile("tempImage", "png")
            }
               storageRef.getFile(localfile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                binding.imgFotoDePerfil.setImageBitmap(bitmap)
            }
        } catch (exception: Exception) {
// la imagen no existe
        }
    }

    /**
     * Función que permite abrir la galería de imágenes del dispositivo para seleccionar una imagen.
     * Utiliza el método launch de la clase guardarImgCamera para abrir la galería y seleccionar una imagen.
     */
    fun obrirImatge() {
        //Obrim la galeria per seleccionar la imatge  //Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        guardarImgCamera.launch(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
        )
    }
}