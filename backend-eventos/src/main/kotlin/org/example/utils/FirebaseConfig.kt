package org.example.utils


import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    fun init() {
        val serviceAccount = java.io.FileInputStream(
            "C:/Users/hdzka/Desktop/dsm_gestioneventos/gestion-eventos-comunitarios/backend-eventos/src/main/resources/gestion-eventos-comunita-f42ed-firebase-adminsdk-fbsvc-6b76cb637d.json"
        )

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        println("Firebase inicializado correctamente")
    }
}