# Recursos y Sensores del Sistema — RoomTracker

## Resumen

RoomTracker utiliza los siguientes recursos del dispositivo para ofrecer navegación, orientación y comunicación dentro del campus universitario.

---

## 📷 Cámara

**Permiso:** `CAMERA`

**Uso en la app:**
- Capturar imágenes para enviar en los **chats de servicio al cliente** (soporte, cafetería, biblioteca)
- Escanear el **código QR del carnet estudiantil** (lectura)

**Cuándo se activa:** Solo cuando el usuario abre el chat y selecciona adjuntar una foto, o al escanear el carnet.

---

## 🖼️ Galería (Almacenamiento externo)

**Permiso:** `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`

**Uso en la app:**
- Seleccionar imágenes desde la galería para enviar en los **chats de servicio al cliente**
- Adjuntar imagen del **horario físico** como referencia visual

**Cuándo se activa:** Solo cuando el usuario elige adjuntar un archivo desde su galería.

---

## 📍 GPS (Ubicación)

**Permiso:** `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`

> ⚠️ **Nota:** El GPS del dispositivo no se usa para posicionamiento dentro del campus (la señal GPS no es precisa en interiores). La ubicación del usuario dentro del mapa se maneja de forma manual o por puntos de referencia.

**Uso real en la app:**
- Detectar la **posición general** del dispositivo para centrar el mapa al abrirlo
- Referencia de contexto geográfico para el campus

---

## 💡 Sensor de Luz (Ambient Light)

**Permiso:** No requiere permiso especial

**Uso en la app:**
- Cuando el nivel de luz ambiental baja, el **mapa cambia automáticamente a modo oscuro**
- Mejora la legibilidad del mapa en condiciones de poca luz

**Cuándo se activa:** Continuamente mientras el mapa está abierto.

---

## 🧲 Magnetómetro (Brújula)

**Permiso:** No requiere permiso especial

**Uso en la app:**
- Detectar la **orientación del dispositivo respecto al norte magnético**
- Rotar el ícono de dirección del usuario en el mapa
- Detecta cambios en los campos magnéticos de la tierra para actualizar la orientación en tiempo real

**Cuándo se activa:** Cuando el usuario tiene el mapa abierto y activa el modo de orientación.

---

## 📐 Acelerómetro

**Permiso:** No requiere permiso especial

**Uso en la app:**
- Detectar si el teléfono está en posición **vertical, horizontal o inclinado**
- Combinar con el magnetómetro para mejorar el cálculo de orientación del usuario
- Ajustar la vista del mapa según la inclinación del dispositivo

**Cuándo se activa:** Mientras el mapa está abierto.

---

## 🔄 Giroscopio

**Permiso:** No requiere permiso especial

**Uso en la app:**
- Complementa el **rotation vector sensor** junto con el acelerómetro y magnetómetro
- Suaviza el movimiento del ícono de orientación del usuario en el mapa (elimina el temblor)
- Hace la rotación del ícono más fluida y precisa

**Cuándo se activa:** Mientras el mapa está abierto y el modo de orientación está activo.

---

## 💾 Almacenamiento Interno

**Permiso:** No requiere permiso especial (filesDir es privado)

**Uso en la app:**
- Guardar en caché los archivos del mapa del campus:
  - `graph.json` — grafo de rutas del campus
  - `campus_updated.geojson` — capas visuales del mapa
  - `edge_geometry.json` — geometría de los caminos
- Los archivos se descargan desde Supabase Storage al iniciar sesión y se reutilizan en sesiones posteriores sin volver a descargar

**Cuándo se activa:** Al iniciar sesión por primera vez o si los archivos no existen localmente.

---

## 👥 Contactos

**Permiso:** `READ_CONTACTS` *(opcional)*

**Uso en la app:**
- Facilitar la búsqueda de **compañeros de la universidad** para enviar solicitudes de amistad
- No se almacenan los contactos en el servidor, solo se usan localmente para sugerencias

**Cuándo se activa:** Solo si el usuario autoriza y accede a la sección de amigos.

---

## Resumen de Permisos

| Recurso         | Permiso Android                        | 
|-----------------|----------------------------------------|
| Cámara          | `CAMERA`                               |
| Galería         | `READ_MEDIA_IMAGES`                    | 
| GPS             | `ACCESS_FINE_LOCATION`                 | 
| Luz             | Sin permiso                            | 
| Magnetómetro    | Sin permiso                            | 
| Acelerómetro    | Sin permiso                            | 
| Giroscopio      | Sin permiso                            | 
| Almacenamiento  | Sin permiso (filesDir privado)         | 

