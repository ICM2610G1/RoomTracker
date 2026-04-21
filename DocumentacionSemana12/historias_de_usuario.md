# Historias de Usuario — RoomTracker

## Roles
- **Estudiante**: usuario principal de la aplicación
- **Sistema**: acciones automáticas del backend

---

## Épica 1: Autenticación

### HU-01 — Registro de cuenta
**Como** estudiante,  
**quiero** registrarme con mi correo institucional y contraseña,  
**para** crear una cuenta en la plataforma.

**Criterios de aceptación:**
- [ ] El correo debe tener formato válido
- [ ] Si la institución es privada, el correo debe coincidir con el dominio institucional
- [ ] Se envía un código OTP al correo para verificación
- [ ] La contraseña se almacena cifrada (nunca en texto plano)
- [ ] El estado inicial del usuario es `pendiente` hasta verificar el correo

---

### HU-02 — Verificación de correo
**Como** estudiante,  
**quiero** ingresar el código OTP que recibí en mi correo,  
**para** activar mi cuenta y poder iniciar sesión.

**Criterios de aceptación:**
- [ ] El código OTP tiene una fecha de expiración
- [ ] Si el código es incorrecto, se muestra un mensaje de error
- [ ] Al verificar exitosamente, el estado del usuario cambia a `activo`
- [ ] Se puede solicitar reenvío del código

---

### HU-03 — Inicio de sesión
**Como** estudiante,  
**quiero** iniciar sesión con mi correo y contraseña,  
**para** acceder a las funciones de la aplicación.

**Criterios de aceptación:**
- [ ] Solo usuarios con estado `activo` pueden iniciar sesión
- [ ] Se debe seleccionar un campus/institución al iniciar sesión
- [ ] La sesión persiste al cerrar y reabrir la app
- [ ] Se muestra mensaje de error si las credenciales son incorrectas
- [ ] Se reintenta automáticamente hasta 3 veces en caso de error de red

---

### HU-04 — Recuperación de contraseña
**Como** estudiante,  
**quiero** recuperar el acceso a mi cuenta si olvidé mi contraseña,  
**para** poder volver a usar la aplicación.

**Criterios de aceptación:**
- [ ] Se ingresa el correo registrado y se envía un OTP de recuperación
- [ ] El token de recuperación tiene fecha de expiración
- [ ] Solo se puede usar el token una vez (`usado = true` tras usarse)
- [ ] Tras cambiar la contraseña, se cierra la sesión activa
- [ ] Se redirige al login después del restablecimiento exitoso

---

### HU-05 — Cierre de sesión
**Como** estudiante,  
**quiero** cerrar sesión desde la aplicación,  
**para** proteger mi cuenta en dispositivos compartidos.

**Criterios de aceptación:**
- [ ] Al cerrar sesión se elimina la sesión activa del dispositivo
- [ ] Se redirige a la pantalla de login
- [ ] Los archivos del mapa del campus se eliminan localmente

---

## Épica 2: Mapa del Campus

### HU-06 — Ver mapa del campus
**Como** estudiante,  
**quiero** ver el mapa interactivo de mi campus,  
**para** orientarme dentro de la universidad.

**Criterios de aceptación:**
- [ ] El mapa carga las capas GeoJSON correspondientes a la institución del usuario
- [ ] Se muestran edificios, senderos y puntos de interés
- [ ] El mapa es interactivo (zoom, desplazamiento)
- [ ] Si los archivos no están descargados, se descargan automáticamente al iniciar sesión

---

### HU-07 — Navegar a un destino
**Como** estudiante,  
**quiero** buscar un destino en el campus y obtener una ruta,  
**para** llegar fácilmente sin perderme.

**Criterios de aceptación:**
- [ ] Se puede buscar un espacio por nombre
- [ ] El sistema calcula la ruta más corta usando el grafo del campus
- [ ] Se visualiza la ruta en el mapa
- [ ] Se muestran rutas alternativas si están disponibles

---

### HU-08 — Ver información de un espacio
**Como** estudiante,  
**quiero** tocar un edificio o espacio en el mapa y ver su información,  
**para** conocer qué hay en ese lugar.

**Criterios de aceptación:**
- [ ] Al seleccionar un espacio se muestra nombre, descripción y tipo
- [ ] Se puede ver el contenido multimedia del espacio (imágenes, videos, links)
- [ ] Si el espacio tiene link externo (ej. reserva de cubículo), se puede abrir
- [ ] Se muestra el piso en el que se encuentra

---

### HU-09 — Ver plano de evacuación de un piso
**Como** estudiante,  
**quiero** ver el plano de evacuación del piso en el que me encuentro,  
**para** saber cómo actuar en una emergencia.

**Criterios de aceptación:**
- [ ] Cada piso puede tener una imagen de plano de evacuación
- [ ] El plano se puede visualizar en pantalla completa
- [ ] Si no existe plano cargado, se indica que no está disponible

---

## Épica 3: Horario

### HU-10 — Crear una clase en mi horario
**Como** estudiante,  
**quiero** agregar mis clases a un horario semanal,  
**para** tener organizado mi calendario académico dentro de la app.

**Criterios de aceptación:**
- [ ] Se puede ingresar título, día de la semana, hora inicio y hora fin
- [ ] Opcionalmente se puede asociar la clase a un espacio/salón del campus
- [ ] Se puede subir una imagen del horario físico como referencia
- [ ] El horario puede tener múltiples entradas por día

---

### HU-11 — Recibir notificación antes de una clase
**Como** estudiante,  
**quiero** recibir una notificación X minutos antes de que empiece una clase,  
**para** no llegar tarde.

**Criterios de aceptación:**
- [ ] Se puede configurar cuántos minutos antes recibir la notificación (ej: 15 min)
- [ ] La notificación se genera automáticamente según el horario
- [ ] Si el salón tiene ubicación en el mapa, la notificación puede incluir la ruta
- [ ] La notificación puede marcarse como leída

---

## Épica 4: Social

### HU-12 — Enviar solicitud de amistad
**Como** estudiante,  
**quiero** agregar a otros estudiantes como amigos,  
**para** conectarme con compañeros de la universidad.

**Criterios de aceptación:**
- [ ] Se puede enviar una solicitud de amistad a otro usuario
- [ ] El destinatario puede aceptar, rechazar o bloquear la solicitud
- [ ] No pueden existir pares duplicados (usuario_1, usuario_2)
- [ ] Se garantiza orden único: `id_usuario_1 < id_usuario_2`

---

### HU-13 — Chatear con soporte o servicios del campus
**Como** estudiante,  
**quiero** abrir un chat según el contexto (soporte, cafetería, biblioteca),  
**para** resolver dudas o hacer consultas rápidamente.

**Criterios de aceptación:**
- [ ] El chat está asociado a la sesión activa del usuario
- [ ] Se pueden enviar mensajes de tipo texto, imagen o video
- [ ] Los archivos adjuntos se almacenan con su URL, tipo MIME y tamaño
- [ ] Un usuario puede tener múltiples chats activos

---

## Épica 5: Carnet Digital

### HU-14 — Ver mi carnet estudiantil digital
**Como** estudiante,  
**quiero** ver mi carnet digital dentro de la app,  
**para** identificarme sin necesitar el carnet físico.

**Criterios de aceptación:**
- [ ] Se muestra el código de estudiante y código QR
- [ ] Solo usuarios de tipo `estudiante` tienen carnet
- [ ] Cada usuario tiene máximo 1 carnet (relación 1-1)
- [ ] El carnet puede estar activo o inactivo

---

## Épica 6: Eventos

### HU-15 — Ver eventos del campus
**Como** estudiante,  
**quiero** ver los eventos que organiza mi universidad,  
**para** enterarme de actividades culturales, académicas y deportivas.

**Criterios de aceptación:**
- [ ] Se listan los eventos activos de la institución del usuario
- [ ] Cada evento muestra título, fecha, tipo e imagen
- [ ] Los eventos tienen fecha inicio y fecha fin válidas (`fecha_inicio < fecha_fin`)
- [ ] Se puede filtrar por tipo (charla, taller, deportivo, cultural, académico)

---

### HU-16 — Navegar al lugar de un evento
**Como** estudiante,  
**quiero** obtener la ruta hacia el lugar de un evento desde el mapa,  
**para** llegar fácilmente al sitio.

**Criterios de aceptación:**
- [ ] Si el evento tiene un espacio asociado, se puede iniciar navegación desde la pantalla del evento
- [ ] La ruta se visualiza en el mapa del campus
- [ ] Si el evento es virtual, no se muestra opción de ruta

---

## Épica 7: Oportunidades

### HU-17 — Ver oportunidades laborales y becas
**Como** estudiante,  
**quiero** ver las oportunidades de empleo, prácticas y becas disponibles,  
**para** postularme a convocatorias relevantes para mí.

**Criterios de aceptación:**
- [ ] Se listan oportunidades activas de la institución
- [ ] Cada oportunidad muestra título, tipo, requisitos y fecha de cierre
- [ ] Se puede acceder al link externo de inscripción
- [ ] Se puede filtrar por tipo (empleo, práctica, beca)

---

## Épica 8: Menú de Cafetería

### HU-18 — Ver el menú de la cafetería
**Como** estudiante,  
**quiero** ver el menú disponible en la cafetería del campus,  
**para** saber qué puedo comer sin tener que ir hasta allá.

**Criterios de aceptación:**
- [ ] Se muestran los ítems de menú del espacio tipo cafetería
- [ ] Cada ítem muestra nombre, tipo, precio e imagen
- [ ] Se puede filtrar por tipo (desayuno, almuerzo, cena, snack, bebidas)
- [ ] Se indica si el ítem está disponible para ese día de la semana
- [ ] El precio siempre es mayor a 0

---

## Épica 9: Tienda Virtual

### HU-19 — Ver productos de la tienda virtual
**Como** estudiante,  
**quiero** ver los productos disponibles en la tienda virtual de la universidad,  
**para** conocer qué artículos puedo adquirir.

**Criterios de aceptación:**
- [ ] Se listan los productos activos de la institución
- [ ] Cada producto muestra nombre, descripción, precio e imagen
- [ ] Se indica si el producto está disponible
- [ ] Si el stock es `null`, se considera ilimitado
- [ ] Si el stock es 0, el producto se muestra como agotado

---

## Resumen

| ID     | Historia                              | Épica               |
|--------|---------------------------------------|---------------------|
| HU-01  | Registro de cuenta                    | Autenticación       |
| HU-02  | Verificación de correo                | Autenticación       |
| HU-03  | Inicio de sesión                      | Autenticación       |
| HU-04  | Recuperación de contraseña            | Autenticación       |
| HU-05  | Cierre de sesión                      | Autenticación       |
| HU-06  | Ver mapa del campus                   | Mapa                |
| HU-07  | Navegar a un destino                  | Mapa                |
| HU-08  | Ver información de un espacio         | Mapa                |
| HU-09  | Ver plano de evacuación               | Mapa                |
| HU-10  | Crear clase en horario                | Horario             |
| HU-11  | Notificación antes de clase           | Horario             |
| HU-12  | Enviar solicitud de amistad           | Social              |
| HU-13  | Chatear con servicios del campus      | Social              |
| HU-14  | Ver carnet digital                    | Carnet              |
| HU-15  | Ver eventos del campus                | Eventos             |
| HU-16  | Navegar al lugar de un evento         | Eventos             |
| HU-17  | Ver oportunidades y becas             | Oportunidades       |
| HU-18  | Ver menú de cafetería                 | Menú                |
| HU-19  | Ver productos de tienda virtual       | Tienda Virtual      |
