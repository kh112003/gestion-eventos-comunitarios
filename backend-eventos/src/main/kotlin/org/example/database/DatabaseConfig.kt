package org.example.org.example.database


import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    fun init() {
        Database.connect(
            url = "jdbc:sqlite:eventos.db",
            driver = "org.sqlite.JDBC"
        )
        transaction {
            exec("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT,
                    proveedor TEXT DEFAULT 'local',
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)
        }
        println("Base de datos inicializada correctamente")
    }
}