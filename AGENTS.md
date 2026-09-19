# AGENTS.md - Ayesha Mart working agreement

Project context and phase history live in `PROGRESS.md`. Read it first when resuming.

## Standing instructions from the user
- After **each phase is completed** (built, deployed and tested), **commit and push to GitHub**
  (`origin` = https://github.com/AyeshaAmren/AyeshaMart.git, branch `main`).
- Commit message style: `Phase N: <short summary>`.
- Do **not** force-push and do not rewrite history.
- Only commit/push when a phase is complete (or when explicitly asked), never mid-phase.

## Build / run / test
- Build: `mvn -B package` -> `target/ayesha-mart.war` (avoid `mvn clean`; OneDrive locks `target/`).
- Deploy: copy the WAR into `C:\tools\apache-tomcat-11.0.5\webapps\`, start Tomcat with
  `$env:AYESHA_MART_DATA_DIR="<dir>"`.
- Test suites in `manual-tests/` (run order: phase5 -> phase4 -> phase3), then stop Tomcat.
- Push may need interactive GitHub auth; if `git push` hangs, ask the user to run it.

## Roadmap
- Phases 1-5 done. Next: Phase 6 (checkout, orders, payment) when the user asks.
