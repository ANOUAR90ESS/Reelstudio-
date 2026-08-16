# Admin Console

Films and episodes ("stories") are authored from an in-app admin console. Viewers cannot reach it
and cannot create content — the console is hidden for them, and the Firestore rules reject their
writes even if they bypass the app.

## Who is an admin

An account is an admin when **any** of these is true:

1. Its Firestore profile (`users/{uid}`) has `role: "admin"`.
2. Its email is listed in `AdminConfig.bootstrapAdminEmails` — the project owners, who have no
   existing admin to promote them. On sign-in these accounts are upgraded to `role: "admin"`
   automatically.
3. The device was unlocked with `AdminConfig.LOCAL_ADMIN_PASSCODE` (default `reel-admin`).

Path 3 is the offline escape hatch: this project ships without a `google-services.json`, so without
it the console would be unreachable on a device that has never signed in. It unlocks the local
editor only, it is not persisted across restarts, and anything authored that way stays on the device
until a real admin account publishes it.

**Edit `app/src/main/java/com/example/data/admin/AdminConfig.kt`** to change the owner list or the
local passcode.

## Where the enforcement actually lives

The client-side check is a UX gate, not a security boundary — anyone can patch an APK. The real
enforcement is `firestore.rules` at the repository root, which only allows writes to `dramas/` and
`dramas/{id}/episodes/` when the caller's `users/{uid}.role` is `admin`, and which stops a user from
promoting themselves.

Deploy it with:

```bash
firebase deploy --only firestore:rules
```

## Using the console

Open **Library → Admin Console** (visible to admins only).

- **Dashboard** — counts across films, drafts and episodes, plus every authored film with its
  actions: Edit, Episodes, Publish/Unpublish, Delete.
- **New Film** — title, synopsis, genre, badge, cover gradient, credits, rating and counters.
  A new film is saved as a **draft** and you land straight in its episode manager.
- **Episodes** — add, edit, reorder-by-number and delete episodes. Each episode carries a duration,
  free/paid access with a coin price, a preview line, and timed script lines.
- **Publish** — a draft is invisible to viewers. Publishing puts the film into Home, Discover and
  For You, and mirrors it (with its episodes) to Firestore so other devices see it.

Deleting a film also removes its episodes and any viewer bookmarks, history and unlocks that pointed
at it, so nothing is left dangling.

## How the data flows

- **Room is the source of truth.** Admin content lives in the `admin_dramas` and `admin_episodes`
  tables (added in database version 2 by an additive migration — existing coins, bookmarks, history
  and unlocks are preserved).
- **Firestore is a best-effort mirror.** A failed push is reported but never rolls back the local
  write, so a flaky connection cannot eat an admin's work.
- The catalog viewers browse is `published admin films + bundled sample films`. Drafts never leave
  the console.
