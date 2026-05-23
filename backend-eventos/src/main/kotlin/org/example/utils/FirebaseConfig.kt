package org.example.utils


import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    fun init() {
        val serviceAccount = javaClass.classLoader
            .getResourceAsStream("gestion-eventos-comunita-f42ed-firebase-adminsdk-fbsvc-6b76cb637d.json")
            ?: error("No se encontro el archivo de credenciales de Firebase en src/main/resources")

        val options = FirebaseOptions.builder()
            .setCredentials(serviceAccount.use { GoogleCredentials.fromStream(it) })
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        println("Firebase inicializado correctamente")
    }
}
