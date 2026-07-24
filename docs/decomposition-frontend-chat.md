# Guide frontend - Connexion au backend Chat

## 1. Objectif

Ce document explique comment connecter un frontend au backend deploye :

```text
https://stage-vmp6.onrender.com
```

Le backend expose une authentification JWT et un module Chat base sur OpenRouter. Le frontend doit :

- inscrire ou connecter un utilisateur ;
- utiliser les valeurs saisies dans les formulaires, sans identifiants hardcodes ;
- stocker le JWT retourne par le backend ;
- envoyer les messages au endpoint `/chat` avec `Authorization: Bearer <token>` ;
- conserver le `conversationId` courant pendant la discussion ;
- afficher les reponses et les erreurs de maniere claire.
- bloquer les pages protegees tant que l'utilisateur n'est pas connecte.

## 2. Configuration frontend

Creer une variable d'environnement pour eviter de dupliquer l'URL dans le code.

### Vite / React

```env
VITE_API_BASE_URL=https://stage-vmp6.onrender.com
```

Utilisation :

```ts
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "https://stage-vmp6.onrender.com";
```

### Next.js

```env
NEXT_PUBLIC_API_BASE_URL=https://stage-vmp6.onrender.com
```

Utilisation :

```ts
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "https://stage-vmp6.onrender.com";
```

## 3. Authentification

Le module Chat est protege. Il faut d'abord appeler `/auth/register` ou `/auth/login`.

Important : le frontend ne doit pas hardcoder `username` ou `password` dans le code de login. Les valeurs doivent venir des champs du formulaire.

### Inscription

```http
POST https://stage-vmp6.onrender.com/auth/register
Content-Type: application/json
```

```json
{
  "username": "<valeur-du-formulaire>",
  "password": "<valeur-du-formulaire>"
}
```

Reponse :

```json
{
  "token": "jwt-token",
  "username": "<username>"
}
```

### Connexion

```http
POST https://stage-vmp6.onrender.com/auth/login
Content-Type: application/json
```

```json
{
  "username": "<valeur-du-formulaire>",
  "password": "<valeur-du-formulaire>"
}
```

Reponse :

```json
{
  "token": "jwt-token",
  "username": "<username>"
}
```

Le frontend peut stocker le token dans `localStorage` pour un projet simple :

```ts
localStorage.setItem("token", data.token);
localStorage.setItem("username", data.username);
```

Pour une application de production, preferer une strategie plus stricte contre XSS, par exemple cookie HTTP-only si le backend est adapte.

## 4. Routes et acces frontend

Avant connexion, l'utilisateur ne doit pouvoir utiliser que :

- `Home` ;
- `Login` ;
- `Register` si l'inscription est exposee dans l'interface.

Toutes les autres pages doivent etre protegees :

- `Chat` ;
- tableau de bord ;
- pages participants, formateurs, cycles ou autres donnees metier ;
- toute action qui appelle une route backend protegee.

Regles frontend :

- si aucun token n'existe, rediriger vers `/login` ;
- si le token est invalide ou expire, supprimer le token local et rediriger vers `/login` ;
- ne jamais afficher le formulaire Chat a un utilisateur non connecte ;
- ne jamais envoyer une requete `/chat` sans `Authorization: Bearer <token>`.

Exemple de protection de route :

```tsx
import { Navigate } from "react-router-dom";

type ProtectedRouteProps = {
  children: React.ReactNode;
};

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
```

Exemple d'utilisation :

```tsx
<Route path="/" element={<HomePage />} />
<Route path="/login" element={<LoginPage />} />
<Route path="/register" element={<RegisterPage />} />
<Route
  path="/chat"
  element={
    <ProtectedRoute>
      <ChatPage />
    </ProtectedRoute>
  }
/>
```

## 5. Endpoint Chat

### Requete

```http
POST https://stage-vmp6.onrender.com/chat
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Premier message :

```json
{
  "message": "Bonjour, peux-tu m'aider ?"
}
```

Messages suivants dans la meme conversation :

```json
{
  "conversationId": "uuid-retourne-par-le-backend",
  "message": "Continue avec plus de details"
}
```

Regles importantes :

- `message` est obligatoire et ne doit pas etre vide.
- `conversationId` est optionnel au premier message.
- Si `conversationId` est absent ou vide, le backend cree une nouvelle conversation.
- Le backend separe les conversations par utilisateur authentifie.
- L'historique est garde en memoire cote backend, avec une limite de 20 messages.
- Si le serveur redemarre, l'historique cote backend est perdu.

### Reponse

```json
{
  "conversationId": "uuid-de-la-conversation",
  "message": "Reponse de l'assistant"
}
```

Le frontend doit sauvegarder `conversationId` apres chaque reponse et le renvoyer au prochain message.

## 6. Types TypeScript recommandes

```ts
export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  username: string;
};

export type ChatRequest = {
  conversationId?: string | null;
  message: string;
};

export type ChatResponse = {
  conversationId: string;
  message: string;
};

export type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  createdAt: string;
  status?: "sending" | "sent" | "error";
};
```

## 7. Service API

Creer un fichier comme `src/api/backendApi.ts`.

```ts
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "https://stage-vmp6.onrender.com";

async function readJsonResponse<T>(response: Response): Promise<T> {
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message =
      data?.message ??
      data?.error ??
      `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return data as T;
}

export async function login(
  username: string,
  password: string
): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  return readJsonResponse<LoginResponse>(response);
}

export async function register(
  username: string,
  password: string
): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  return readJsonResponse<LoginResponse>(response);
}

export async function sendChatMessage(
  request: ChatRequest,
  token: string
): Promise<ChatResponse> {
  const response = await fetch(`${API_BASE_URL}/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      message: request.message,
      ...(request.conversationId
        ? { conversationId: request.conversationId }
        : {}),
    }),
  });

  return readJsonResponse<ChatResponse>(response);
}
```

Les fonctions `login` et `register` recoivent `username` et `password` en parametres. Ces valeurs doivent venir du state du formulaire, par exemple `useState`, React Hook Form ou Formik. Ne pas mettre de valeurs comme `"demo"` ou `"demo123"` dans le code applicatif.

Exemple minimal de formulaire login :

```tsx
import { FormEvent, useState } from "react";
import { login } from "../api/backendApi";

export function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const data = await login(username, password);
      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
    } catch (exception) {
      setError(
        exception instanceof Error
          ? exception.message
          : "Connexion impossible."
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <input
        value={username}
        onChange={(event) => setUsername(event.target.value)}
        placeholder="Nom utilisateur"
        autoComplete="username"
      />
      <input
        value={password}
        onChange={(event) => setPassword(event.target.value)}
        placeholder="Mot de passe"
        type="password"
        autoComplete="current-password"
      />
      <button disabled={isLoading || !username || !password} type="submit">
        Se connecter
      </button>
      {error ? <p>{error}</p> : null}
    </form>
  );
}
```

## 8. Hook React propose

Creer un fichier comme `src/hooks/useChat.ts`.

```ts
import { useState } from "react";
import { sendChatMessage } from "../api/backendApi";

export function useChat(token: string | null) {
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function sendMessage(message: string) {
    const cleanMessage = message.trim();

    if (!cleanMessage) {
      setError("Le message ne peut pas etre vide.");
      return;
    }

    if (!token) {
      setError("Vous devez etre connecte pour utiliser le chat.");
      return;
    }

    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: "user",
      content: cleanMessage,
      createdAt: new Date().toISOString(),
      status: "sending",
    };

    setMessages((current) => [...current, userMessage]);
    setIsLoading(true);
    setError(null);

    try {
      const response = await sendChatMessage(
        { conversationId, message: cleanMessage },
        token
      );

      setConversationId(response.conversationId);
      setMessages((current) => [
        ...current.map((item) =>
          item.id === userMessage.id ? { ...item, status: "sent" } : item
        ),
        {
          id: crypto.randomUUID(),
          role: "assistant",
          content: response.message,
          createdAt: new Date().toISOString(),
          status: "sent",
        },
      ]);
    } catch (exception) {
      const message =
        exception instanceof Error
          ? exception.message
          : "Erreur pendant l'envoi du message.";

      setError(message);
      setMessages((current) =>
        current.map((item) =>
          item.id === userMessage.id ? { ...item, status: "error" } : item
        )
      );
    } finally {
      setIsLoading(false);
    }
  }

  function resetConversation() {
    setConversationId(null);
    setMessages([]);
    setError(null);
  }

  return {
    conversationId,
    messages,
    isLoading,
    error,
    sendMessage,
    resetConversation,
  };
}
```

## 9. Interface attendue

Le composant UI du chat peut consommer cette interface :

```ts
type UseChatResult = {
  conversationId: string | null;
  messages: ChatMessage[];
  isLoading: boolean;
  error: string | null;
  sendMessage: (message: string) => Promise<void>;
  resetConversation: () => void;
};
```

Composants recommandes :

```text
ChatPage
ChatWindow
MessageList
MessageBubble
ChatInput
ChatError
ChatLoadingIndicator
```

Comportements importants :

- afficher les messages dans l'ordre chronologique ;
- differencier visuellement les messages utilisateur et assistant ;
- desactiver le bouton d'envoi si l'input est vide ;
- afficher un etat de chargement pendant la reponse ;
- scroller automatiquement vers le dernier message ;
- proposer un bouton "Nouvelle conversation" qui appelle `resetConversation()`.

## 10. Gestion des erreurs

Le backend peut retourner ces erreurs principales.

### Validation

Status : `400`

```json
{
  "message": "Validation failed",
  "errors": {
    "message": "must not be blank"
  }
}
```

Action frontend : afficher un message simple, par exemple `Le message est obligatoire.`

### Non authentifie

Status : `401` ou `403`

Cause probable :

- token absent ;
- token invalide ;
- token expire ;
- mauvais format de header.

Action frontend : rediriger vers login ou afficher `Session expiree, reconnectez-vous.`

### Backend ou OpenRouter indisponible

Status : `503`

Exemples :

```json
{
  "message": "Could not connect to OpenRouter"
}
```

```json
{
  "message": "OpenRouter API key is not configured"
}
```

Action frontend : afficher `Le service IA est temporairement indisponible.`

## 11. Test rapide avec curl

### Login

```bash
curl -X POST "https://stage-vmp6.onrender.com/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"<username>\",\"password\":\"<password>\"}"
```

### Chat

```bash
curl -X POST "https://stage-vmp6.onrender.com/chat" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d "{\"message\":\"Bonjour\"}"
```

## 12. Checklist de livraison

- `API_BASE_URL` pointe vers `https://stage-vmp6.onrender.com`.
- Le frontend sait faire login ou register.
- Les identifiants ne sont jamais hardcodes dans le code frontend.
- Les champs login/register utilisent les valeurs saisies par l'utilisateur.
- Sans token, l'utilisateur peut seulement acceder a Home, Login et Register.
- Les pages protegees redirigent vers `/login` si l'utilisateur n'est pas connecte.
- Le token JWT est ajoute dans `Authorization`.
- Le premier message est envoye sans `conversationId`.
- Les messages suivants reutilisent le `conversationId` retourne.
- L'interface gere les etats `loading`, `error`, `empty` et `authenticated`.
- Le bouton "Nouvelle conversation" remet `conversationId` a `null`.
- Les erreurs `400`, `401/403` et `503` ont un message utilisateur clair.
- Les tests manuels sont faits sur desktop et mobile.

## 13. Notes backend

Le backend utilise OpenRouter via `POST /chat/completions`. Dans `application.properties`, le modele par defaut est :

```properties
openrouter.model=${OPENROUTER_MODEL:openrouter/free}
```

Les variables serveur importantes sont :

```text
OPENROUTER_API_KEY=<cle-api-openrouter>
OPENROUTER_MODEL=openrouter/free
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
```

La cle OpenRouter doit rester cote backend. Elle ne doit jamais etre exposee dans le frontend.
