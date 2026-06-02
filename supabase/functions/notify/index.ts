// Supabase Edge Function — notify
// Recibe webhooks de la BD y envía notificaciones push via FCM HTTP v1
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL      = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const FIREBASE_PROJECT_ID  = Deno.env.get("FIREBASE_PROJECT_ID")!;
const SERVICE_ACCOUNT_JSON = Deno.env.get("FIREBASE_SERVICE_ACCOUNT")!;

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

// ─── Entrada principal ────────────────────────────────────────────────────────
Deno.serve(async (req) => {
  try {
    const body = await req.json();
    const { type, table, record, old_record } = body;

    console.log(`Webhook recibido: ${type} en ${table}`);

    if (table === "amistad") {
      await handleAmistad(type, record, old_record);
    } else if (table === "mensaje") {
      await handleMensaje(type, record);
    }

    return new Response(JSON.stringify({ ok: true }), { status: 200 });
  } catch (err) {
    console.error("Error en notify:", err);
    return new Response(JSON.stringify({ error: String(err) }), { status: 500 });
  }
});

// ─── Lógica por tabla ─────────────────────────────────────────────────────────

async function handleAmistad(type: string, record: any, old_record: any) {
  if (type === "INSERT" && record.estado === "pendiente") {
    // Nueva solicitud de amistad → notificar al destinatario (id_usuario_2)
    const nombre = await getNombre(record.id_usuario_1);
    await sendToUser(
      record.id_usuario_2,
      "Nueva solicitud de amistad 👋",
      `${nombre} te envió una solicitud de amistad`,
      { tipo: "solicitud", amistad_id: record.id_amistad }
    );
  } else if (type === "UPDATE" && record.estado === "aceptada" && old_record?.estado === "pendiente") {
    // Solicitud aceptada → notificar al que la envió (id_usuario_1)
    const nombre = await getNombre(record.id_usuario_2);
    await sendToUser(
      record.id_usuario_1,
      "Solicitud aceptada ✅",
      `${nombre} aceptó tu solicitud de amistad`,
      { tipo: "solicitud_aceptada", amistad_id: record.id_amistad }
    );
  }
}

async function handleMensaje(type: string, record: any) {
  if (type !== "INSERT") return;

  // Buscar a quién pertenece el chat
  const { data: chat } = await supabase
    .from("chat")
    .select("id_usuario, contexto_id")
    .eq("id_chat", record.id_chat)
    .single();

  if (!chat) return;

  const estudianteId = chat.id_usuario;
  const emisorId     = record.id_emisor;

  if (emisorId === estudianteId) {
    // El estudiante escribió → notificar al operador del chat
    const { data: chatData } = await supabase
      .from("chat")
      .select("contexto_id")
      .eq("id_chat", record.id_chat)
      .single();

    if (!chatData?.contexto_id) return;

    const nombreEst = await getNombre(estudianteId);
    const preview   = record.tipo === "imagen" ? "📷 Imagen" : (record.contenido_texto ?? record.contenido ?? "");

    await sendToUser(
      chatData.contexto_id,
      `Mensaje de ${nombreEst}`,
      preview.length > 80 ? preview.substring(0, 80) + "…" : preview,
      { tipo: "mensaje", chat_id: record.id_chat }
    );
    return;
  }

  // El operador escribió → notificar al estudiante
  const nombreOp = await getNombreOperador(emisorId);
  const preview  = record.tipo === "imagen" ? "📷 Imagen" : (record.contenido ?? "");

  await sendToUser(
    estudianteId,
    `Mensaje de ${nombreOp}`,
    preview.length > 80 ? preview.substring(0, 80) + "…" : preview,
    { tipo: "mensaje", chat_id: record.id_chat }
  );
}

// ─── Helpers de base de datos ─────────────────────────────────────────────────

async function getNombre(userId: string): Promise<string> {
  const { data } = await supabase
    .from("usuarios")
    .select("nombre, apellido")
    .eq("id_usuario", userId)
    .single();
  return data ? `${data.nombre} ${data.apellido}` : "Alguien";
}

async function getNombreOperador(userId: string): Promise<string> {
  const { data } = await supabase
    .from("usuarios")
    .select("nombre")
    .eq("id_usuario", userId)
    .single();
  return data?.nombre ?? "Soporte";
}

async function getFCMToken(userId: string): Promise<string | null> {
  const { data } = await supabase
    .from("usuarios")
    .select("fcm_token")
    .eq("id_usuario", userId)
    .single();
  return data?.fcm_token ?? null;
}

// ─── Envío FCM HTTP v1 ────────────────────────────────────────────────────────

async function sendToUser(
  userId: string,
  title: string,
  body: string,
  data: Record<string, string>
) {
  const token = await getFCMToken(userId);
  if (!token) {
    console.log(`Sin token FCM para userId=${userId}`);
    return;
  }

  const accessToken = await getFirebaseAccessToken();

  const message = {
    message: {
      token,
      notification: { title, body },
      data: Object.fromEntries(Object.entries(data).map(([k, v]) => [k, String(v)])),
      android: {
        priority: "high",
        notification: { sound: "default", channel_id: "roomtracker_channel" },
      },
    },
  };

  const res = await fetch(
    `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(message),
    }
  );

  const json = await res.json();
  console.log("FCM response:", JSON.stringify(json));
}

// ─── Auth con Service Account (JWT → OAuth token) ────────────────────────────

async function getFirebaseAccessToken(): Promise<string> {
  const sa  = JSON.parse(SERVICE_ACCOUNT_JSON);
  const now = Math.floor(Date.now() / 1000);

  const headerB64  = toBase64Url(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const payloadB64 = toBase64Url(JSON.stringify({
    iss  : sa.client_email,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud  : "https://oauth2.googleapis.com/token",
    iat  : now,
    exp  : now + 3600,
  }));

  const signingInput = `${headerB64}.${payloadB64}`;
  const privateKey   = await importPrivateKey(sa.private_key);

  const signatureBytes = await crypto.subtle.sign(
    { name: "RSASSA-PKCS1-v1_5" },
    privateKey,
    new TextEncoder().encode(signingInput)
  );

  const signatureB64 = toBase64UrlBytes(new Uint8Array(signatureBytes));
  const jwt          = `${signingInput}.${signatureB64}`;

  const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
    method : "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body   : `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`,
  });

  const { access_token } = await tokenRes.json();
  return access_token;
}

async function importPrivateKey(pem: string): Promise<CryptoKey> {
  const pemClean = pem
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s/g, "");

  const binaryDer = Uint8Array.from(atob(pemClean), (c) => c.charCodeAt(0));

  return await crypto.subtle.importKey(
    "pkcs8",
    binaryDer.buffer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
}

function toBase64Url(str: string): string {
  return btoa(unescape(encodeURIComponent(str)))
    .replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}

function toBase64UrlBytes(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes))
    .replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}
