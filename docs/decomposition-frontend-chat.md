# Decomposition frontend du module Chat

## 1. Objectif

Ce document decrit la decomposition du frontend a developper pour consommer le backend du module Chat base sur `ChatService.java`.

Le backend expose une fonctionnalite de conversation avec un assistant IA via OpenRouter, configure par defaut avec le modele `openai/gpt-oss-120b:free`. Le frontend doit permettre a un utilisateur connecte d'envoyer des messages, recevoir les reponses de l'assistant, conserver le `conversationId` courant et gerer les erreurs de communication.

## 2. Fonctionnement backend a respecter

### Endpoint principal

```http
POST /chat
Authorization: Bearer <jwt>
Content-Type: application/json
```

### Corps de la requete

```json
{
  "conversationId": "optionnel",
  "message": "Bonjour"
}
```

Regles importantes :

- `message` est obligatoire.
- `conversationId` est optionnel au premier message.
- Si `conversationId` est absent ou vide, le backend cree une nouvelle conversation.
- Le backend utilise l'utilisateur authentifie pour separer les conversations.
- L'historique est garde cote backend en memoire, avec une limite de 20 messages.

### Reponse attendue

```json
{
  "conversationId": "uuid-de-la-conversation",
  "message": "Reponse de l'assistant"
}
```

### Erreurs possibles

```json
{
  "message": "Validation failed",
  "errors": {
    "message": "must not be blank"
  }
}
```

```json
{
  "message": "Could not connect to OpenRouter"
}
```

Le frontend doit donc gerer :

- erreur 400 si le message est vide ;
- erreur 401/403 si l'utilisateur n'est pas authentifie ;
- erreur 503 si OpenRouter est indisponible ou si la cle API n'est pas configuree ;
- etat de chargement pendant l'attente de la reponse.

### Configuration IA cote backend

Le backend appelle l'API OpenRouter `POST /chat/completions` avec le modele configure dans `openrouter.model`.

Variables d'environnement attendues :

```text
OPENROUTER_API_KEY=<cle-api-openrouter>
OPENROUTER_MODEL=openai/gpt-oss-120b:free
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
```

La cle API doit rester dans une variable d'environnement locale ou dans un gestionnaire de secrets. Elle ne doit pas etre ajoutee dans le code source ni dans ce document.

## 3. Perimetre frontend

Le frontend du module Chat peut etre decoupe en quatre blocs :

1. Interface utilisateur de conversation.
2. Integration API avec le backend.
3. Gestion de l'etat de conversation.
4. Gestion de l'authentification, des erreurs et des tests.

## 4. Repartition sur deux personnes

## Personne 1 : Interface Chat et experience utilisateur

### Responsabilites

La premiere personne est responsable de la partie visible par l'utilisateur.

Elle doit developper :

- la page ou le composant principal du chat ;
- la zone d'affichage des messages utilisateur et assistant ;
- le champ de saisie du message ;
- le bouton d'envoi ;
- l'etat de chargement pendant que l'assistant repond ;
- l'affichage des erreurs simples dans l'interface ;
- le comportement responsive pour desktop et mobile.

### Composants proposes

```text
ChatPage
ChatWindow
MessageList
MessageBubble
ChatInput
ChatError
ChatLoadingIndicator
```

### Taches detaillees

- Creer l'ecran principal du chat.
- Afficher les messages dans l'ordre chronologique.
- Distinguer visuellement les messages de l'utilisateur et ceux de l'assistant.
- Desactiver le bouton d'envoi lorsque le message est vide.
- Desactiver l'input pendant l'envoi si necessaire.
- Afficher un indicateur "assistant en train de repondre".
- Faire defiler automatiquement vers le dernier message.
- Prevoir un bouton ou une action "nouvelle conversation" qui remet le `conversationId` a `null` et vide les messages locaux.

### Livrables

- Composants UI du chat.
- Styles CSS ou composants de design.
- Gestion des etats visuels : vide, chargement, erreur, conversation active.
- Tests d'affichage si le projet frontend utilise une librairie de tests.

## Personne 2 : API, etat et integration backend

### Responsabilites

La deuxieme personne est responsable de la logique technique entre le frontend et le backend.

Elle doit developper :

- le service API qui appelle `POST /chat` ;
- l'ajout du token JWT dans l'en-tete `Authorization` ;
- la gestion du `conversationId` retourne par le backend ;
- la gestion de l'etat local de la conversation ;
- la gestion centralisee des erreurs backend ;
- les tests de l'integration API.

### Fichiers ou modules proposes

```text
api/chatApi
hooks/useChat
types/chat
utils/apiError
auth/tokenStorage
```

### Types frontend proposes

```ts
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

### Taches detaillees

- Creer une fonction `sendChatMessage(request)` qui appelle le backend.
- Ajouter le token JWT dans la requete.
- Envoyer `conversationId` uniquement s'il existe deja.
- Recuperer `conversationId` depuis la reponse et le stocker cote frontend.
- Ajouter le message utilisateur dans l'etat local avant l'appel API.
- Ajouter la reponse assistant apres le retour API.
- Gerer les erreurs :
  - message vide ;
  - utilisateur non connecte ;
  - serveur indisponible ;
  - OpenRouter indisponible ou cle API manquante.
- Exposer une fonction `resetConversation()` pour commencer une nouvelle conversation.

### Exemple de service API

```ts
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
    body: JSON.stringify(request),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Erreur lors de l'envoi du message");
  }

  return data;
}
```

### Livrables

- Service API du chat.
- Hook ou store de gestion de conversation.
- Types TypeScript si le frontend utilise TypeScript.
- Gestion des erreurs backend.
- Tests unitaires du service API et du hook/store.

## 5. Contrat entre les deux personnes

Pour eviter les conflits, les deux personnes doivent se mettre d'accord sur une interface commune.

### Interface proposee pour le composant UI

```ts
type UseChatResult = {
  messages: ChatMessage[];
  isLoading: boolean;
  error: string | null;
  sendMessage: (message: string) => Promise<void>;
  resetConversation: () => void;
};
```

La personne 1 utilise cette interface sans connaitre les details de l'API.

La personne 2 implemente cette interface avec l'appel backend reel.

## 6. Scenario utilisateur principal

1. L'utilisateur ouvre la page Chat.
2. Il ecrit un message.
3. Il clique sur envoyer.
4. Le frontend ajoute le message utilisateur dans la liste.
5. Le frontend appelle `POST /chat`.
6. Le backend cree ou reutilise une conversation.
7. Le backend appelle OpenRouter.
8. Le backend retourne `conversationId` et `message`.
9. Le frontend stocke le `conversationId`.
10. Le frontend affiche la reponse de l'assistant.

## 7. Points d'attention

- Le backend ne fournit pas d'endpoint pour lister les conversations.
- Le backend ne persiste pas l'historique en base de donnees.
- Si le serveur redemarre, l'historique des conversations est perdu.
- Le frontend peut garder les messages localement pour l'affichage, mais la memoire officielle de la conversation est cote backend pendant l'execution du serveur.
- Le module necessite un utilisateur authentifie, car `/chat` est protege par JWT.
- La reponse peut prendre plusieurs secondes, car le backend attend OpenRouter avec un timeout de 60 secondes.

## 8. Planning propose

### Jour 1

- Personne 1 : maquette et composants UI principaux.
- Personne 2 : service API, types et hook/store `useChat`.

### Jour 2

- Personne 1 : integration du hook dans l'interface.
- Personne 2 : gestion des erreurs, JWT et tests API.

### Jour 3

- Integration finale.
- Tests manuels avec backend.
- Correction responsive et amelioration des messages d'erreur.

## 9. Definition de termine

Le module frontend est termine lorsque :

- un utilisateur connecte peut envoyer un message ;
- la reponse de l'assistant s'affiche correctement ;
- le meme `conversationId` est reutilise pendant la conversation ;
- une nouvelle conversation peut etre demarree ;
- les erreurs principales sont affichees clairement ;
- l'interface reste utilisable sur mobile et desktop ;
- les appels API utilisent bien le token JWT.
