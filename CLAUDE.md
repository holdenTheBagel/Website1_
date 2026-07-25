# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A personal services/portfolio website for Holden Case (recent IT grad) — Spring Boot 3 + Thymeleaf, four pages (home, services, about, contact), with a contact form that persists to a database and emails a notification. Deployed on Render (Docker-based) at `hmctechsolutions.com`, source on GitHub at `holdenTheBagel/Website1_claude`.

## Commands

This project uses the Maven Wrapper — no local Maven install required.

```bash
./mvnw spring-boot:run        # run locally (http://localhost:8080)
./mvnw test                   # run all tests
./mvnw test -Dtest=ContactControllerTest              # run a single test class
./mvnw test -Dtest=ContactControllerTest#validSubmissionSavesAndRedirectsWithSuccessFlash  # single test method
./mvnw -DskipTests package    # build the jar (target/portfolio-0.0.1-SNAPSHOT.jar)
```

On Windows use `mvnw.cmd` instead of `./mvnw` outside of Git Bash.

To run with mail notifications working locally, run with the `local` Spring profile active (`-Dspring.profiles.active=local`, already wired into `.vscode/launch.json` for VS Code's Run/Debug button) — this loads `application-local.properties`, which holds the gitignored Gmail SMTP credentials and enables the H2 console. Without that profile, mail sending silently no-ops (logs a warning) rather than failing.

## Architecture

**Request flow**: `PageController` handles the three static pages (`/`, `/services`, `/about`) with plain `return "viewname"` — no model data, just Thymeleaf templates. `ContactController` is the only controller with real logic: `GET /contact` shows the form, `POST /contact` validates via `@Valid ContactForm`, and on success delegates to `ContactService.submit()` before redirecting back to `/contact` (Post-Redirect-Get, using flash attributes `success`/`rateLimited` for the banner shown after redirect).

**Contact form pipeline** (`ContactService.submit()`): honeypot check (silent reject if the hidden `website` field is non-blank) → per-IP rate limit (in-memory `ConcurrentHashMap`, 30s window, resets on restart) → save `ContactMessage` via JPA → fire-and-forget email via `ContactMailNotifier`. The mail notifier is a **separate `@Component`** (not a method on `ContactService`) specifically so its `@Async` annotation is honored — Spring's AOP proxy can't intercept self-invocation, so async methods must live on an injected bean, not be called from within the same class. `PortfolioApplication` carries `@EnableAsync` to activate this. Email failures (including SMTP timeouts, configured at 5s) are logged and swallowed — the DB row is the source of truth if mail fails, never the other way around.

**Templates**: `templates/fragments/layout.html` is never rendered directly — it holds three named fragments (`head(title)`, `header`, `footer`) that every page pulls in via `th:replace`. There's no layout-dialect dependency; this is plain Thymeleaf fragment composition. Adding a new page means adding a template that replaces those three fragments plus one `PageController` method — no other wiring needed.

**Config split between `application.properties` and `application-local.properties`**: the checked-in file has safe production defaults (H2 console disabled, mail credentials empty/unset) using `${ENV_VAR:default}` placeholders for everything environment-specific (`PORT`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CONTACT_NOTIFY_TO`). `application-local.properties` (gitignored) overrides only what's needed for local dev — real Gmail credentials and H2 console re-enabled — via the `local` Spring profile. In production (Render), the same env vars are set directly in Render's dashboard instead of via a profile.

**Persistence**: H2, file-based (`./data/portfolio`), intentionally **not** persisted across Render deploys/restarts (no persistent disk attached — a deliberate cost/simplicity tradeoff, since email is the reliable notification channel and the DB is just a backup/log). Don't add a persistent disk or migrate to Postgres without checking with the user first — this was an explicit decision, not an oversight.

**Deployment**: `Dockerfile` is a two-stage build (JDK image runs `./mvnw package`, JRE image runs the resulting jar) — Render doesn't natively buildpack-detect Java, so Docker is required. `server.port` reads Render's injected `PORT` env var. The JVM is capped with `-XX:MaxRAMPercentage=75` to behave on Render's free/starter instance's limited RAM. Render auto-redeploys on every push to `main`.

## Known constraints worth knowing before changing things

- Outlook/Microsoft consumer SMTP (`smtp-mail.outlook.com`) does **not** work for sending here — Microsoft has disabled basic-auth SMTP for this account (confirmed via the server's own `535 5.7.139` response), so mail sending goes through Gmail SMTP instead, even though the *destination* inbox is still Outlook (`holdencase@outlook.com`, via `contact.notify-to`). Don't "fix" the mail host back to Outlook.
- The contact form's email send **must** stay asynchronous. It used to be synchronous and a slow/unreachable SMTP connection hung the entire HTTP request indefinitely in production — that's why `ContactMailNotifier` exists as a separate `@Async` bean plus explicit SMTP timeouts. Don't inline it back into `ContactService`.
- `/h2-console` must stay disabled in `application.properties` (only enabled in the local profile) — it's an unauthenticated DB admin UI and this app is publicly deployed.
