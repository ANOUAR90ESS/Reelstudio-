# AI production pipeline

The goal is a full pipeline inside the console: **idea → script → video → voice → merge → publish**.
This document records what is built, what is not, and why the missing parts are shaped the way they
are.

## What works today

| Stage | Status | How |
|---|---|---|
| Story & episode outline | **Built** | Gemini via `firebase-ai` (`StoryGenerator.generateStory`) |
| Episode script & subtitles | **Built** | Gemini via `firebase-ai` (`StoryGenerator.generateScript`) |
| Video attached to an episode | **Built (upload)** | Device picker → Firebase Storage, or paste a URL |
| Poster / thumbnail | **Built (upload)** | Device picker → Firebase Storage, or paste a URL |
| Voiceover track | **Built (upload)** | Device picker → Firebase Storage, or paste a URL |
| Real playback | **Built** | Media3 ExoPlayer, falling back to the canvas scene when an episode has no video |
| Video **generation** (Veo, Kling, Seedance, Sora) | **Not built** | Needs a backend — see below |
| Voice **generation** (TTS) | **Not built** | Needs a backend — see below |
| Merging video + voice | **Not built** | Needs a backend — see below |

## Why generation cannot live in the app

Gemini is reachable from the client because `firebase-ai` proxies the call through Firebase using
the app's own credentials. **No other provider works that way.** Veo (via the Gemini API), Kling,
Seedance and Sora each authenticate with a secret API key, and a key shipped inside an APK is not
secret: anyone can unzip the package and read it, then spend the account's credits. There is no
client-side obfuscation that fixes this — the key has to be sent to the provider, so it has to be
present in the app, so it can be extracted.

Two further problems point the same way:

- **Duration.** A video generation takes minutes. An Android process can be killed at any moment
  while it waits; a job that lives on a server survives the phone going into a pocket.
- **Merging.** Muxing a generated video with a generated voice track needs a real media toolchain.
  `ffmpeg-kit` was retired in 2025 and its binaries pulled from Maven; Media3 Transformer can mux on
  device but is fragile across codecs and phone models. Server-side FFmpeg is the reliable answer.

## The shape the backend should take

```
Admin console                  Firestore                   Cloud Function
─────────────                  ─────────                   ──────────────
"generate video"  ──writes──▶  generation_jobs/{id}  ──triggers──▶  onDocumentCreated
                               { status: "queued",                  ├─ reads provider key from
   observes status  ◀──────────  provider, prompt,                  │  functions config (never
   live via listener            episodeId }                         │  in the APK)
                                                                    ├─ calls Veo / Kling /
                                                                    │  Seedance / Sora
                                                                    ├─ polls until the render
                                                                    │  finishes
                                                                    ├─ (optional) generates the
                                                                    │  voice track, then muxes
                                                                    │  both with FFmpeg
                                                                    ├─ uploads the result to
                                                                    │  Storage
                                                                    └─ writes back
                                                                       { status: "ready",
                                                                         videoUrl }
```

The client side of this is small: write a job document, listen to it, and when it turns `ready`
drop the returned URL into the episode's `videoUrl` — exactly where an uploaded file lands today.
That is why the media fields were built first: **generation and upload produce the same thing**, a
URL on an episode, so adding generation later changes nothing downstream.

The rules already assume this: `firestore.rules` restricts catalog writes to admins, and
`storage.rules` restricts media writes to admins, so a job runner writing as the admin's identity
fits without loosening anything.

### What is needed to build it

1. A Firebase project on the **Blaze plan** (Cloud Functions require it, and so does calling any
   external API from a function).
2. An account and API key for whichever provider you choose. They differ substantially — Veo is
   reachable through the Gemini API you are already set up for; Kling, Seedance and Sora each have
   their own account, pricing, queue behaviour and terms.
3. A decision on **one** provider to start with. Building a provider-agnostic runner against four
   different async APIs at once is how this kind of thing stalls; one working path end to end is
   worth more.

## Cost and terms — worth knowing before you start

Generated video is billed per second of output and is not cheap at series scale: a 50-episode
series at 90 seconds each is 75 minutes of generated video. Check each provider's commercial-use
terms too — some restrict redistribution of generated content, which matters for an app that
charges viewers coins to watch it.

## Using what is built

**Write a series:** open the film editor → **Write it with AI** → describe the idea, pick the genre
and episode count → review the draft → **Use this story**. The title, synopsis, tags, cast and
director fill in, and the episode outline is held until the film is saved, where the episode manager
offers to create all of them as drafts in one tap.

**Write one script:** open an episode → **Write this script with AI**. Gemini writes timed dialogue
across the episode's runtime; existing lines are replaced, and a teaser you wrote by hand is kept.

**Attach media:** every media slot offers both *Upload file* (device picker → Firebase Storage with
a progress bar) and *Use URL* (paste a link you already host). Both end in the same field.

Nothing generated is applied on its own — the admin reviews and saves.
