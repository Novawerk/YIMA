# LinkedIn Post

**Title:** I shipped a product without a business model. The product got lighter.

*（Alternates — pick whichever lands best for the audience:）*

- *Not profitable, but meaningful — what shipping YIMA taught me about product weight*
- *What a period app looks like when you remove the business model*
- *I refused to ask "does this help retention?" for an entire product. Here's what happened.*

---

Every product decision I've made in the last decade has been downstream of one question: does this help retention or conversion? I just shipped a product where I refused to ask that question even once. Not as an ideological stance — as a product experiment. The speed was the least interesting part. What stuck with me was how much lighter the product got, almost as if half the weight in it had always been my own instinct to monetize.

YIMA is what came out of it — a period calendar, now live on both stores.

It's also the first thing I've launched under **Novawerk**, the non-profit community I've been building with. Novawerk's tagline is *Not profitable, but meaningful.* YIMA is one of eight projects in the current portfolio, and the first to ship from the cohort I'm part of.

The story is kind of boring. A while back, someone close to me was venting about how bad current period apps are: mandatory signups, ads, core features behind a subscription, cycle data ending up in data-broker pipelines. I said I'd take a look.

Before writing any code I spent an evening actually using the top period apps. The UX wasn't bad. The problem was that the tool part of each app was being slowly strangled to make room for whatever was going to make money next. The tracker was getting smaller every year while the wrapper around it was getting louder.

Concretely, YIMA has no ads, no subscription, no account, no cloud, no analytics, and no business model. Everything is local. The code is open on GitHub.

What refusing the money question looked like in practice: if something was useful, it shipped; if it was annoying, it got cut; and if the user had no reason to open the app, nothing went out to make them. Decisions that would normally take a design review became defaults. I've been writing software a long time and I hadn't shipped anything where the shape of the product felt this obvious in years.

In hindsight that's the Novawerk principle the others get downstream of: *innovation should create real positive change, not chase trends or quick wins.* Easy to nod at on a slide. Different thing to actually build under.

Tech-wise, it's Kotlin Multiplatform and Compose Multiplatform. The main place I had to drop into platform code was the cycle-report export — Canvas on Android, UIKit on iOS — plus the usual thin expect/actual layer for notifications. KMP has stopped feeling like a demo.

One smaller thing I keep coming back to: a lot of the mess in the health-app category traces to one bad assumption, which is that intimate health data is marketing data. It isn't. Once you stop treating it that way, a surprising number of product problems stop existing.

Oh, and about why it's called YIMA — that's actually a really funny one. Every obvious English name in the period-app space is already taken. I burned hours on a thesaurus before giving up and romanizing the Chinese slang: 姨妈 → YIMA. In Chinese, 大姨妈 ("big auntie") is the everyday way to say "my period" — the metaphor being a relative who shows up on her own schedule and stays a few days.

Anyway — if any of the above resonates, here's where to find it:

- App Store: search YIMA or 姨妈来了
- Google Play: same
- Source: github.com/Novawerk/YIMA
- About Novawerk: novawerk.io

Built for anyone who's given up on finding a period app that doesn't treat them like a marketing target. And if the *not profitable, but meaningful* framing lands with you, the rest of what we're making is at novawerk.io.

#BuildInPublic #IndieDev #KotlinMultiplatform #PrivacyByDesign #HealthTech #OpenSource #Novawerk
