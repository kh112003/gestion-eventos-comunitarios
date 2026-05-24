# gestión-eventos-comunitarios
Segundo Proyecto en Android con Kotlin_DSM

## Integrantes

| Nombre                        | Carné    |
|-------------------------------|----------|
| Katya María Hernández Pérez   | HP221350 |
| Melvin Eduardo Robles Rodas   | RR191220 |
| Juan Carlos Ramírez Chávez    | RC231487 |
| David Roberto Ferrer Coto     | FC243112 |


## Licencia
Este proyecto está bajo licencia [CC0-1.0](https://creativecommons.org/publicdomain/zero/1.0/)

## Tecnologías
- Android / Kotlin
- Backend: Kotlin + Ktor
- Base de datos: SQLite
- Autenticación: JWT + Firebase Auth

## Endpoints de la API
- `POST /auth/register` — Registro de usuarios
- `POST /auth/login` — Login con JWT
- `POST /auth/google` — Login con Google
- `GET /eventos` — Ver todos los eventos
- `POST /eventos` — Crear evento
- `PUT /eventos/{id}` — Actualizar evento
- `DELETE /eventos/{id}` — Eliminar evento
- `POST /asistencias` — Confirmar asistencia
- `GET /historial/usuario/{id}` — Historial de eventos
