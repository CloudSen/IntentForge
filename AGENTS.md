# AGENTS.md

## Read docs before coding, Update docs after finish work
Read relevant documents in `docs/` before making changes:

- `docs/architecture/module-map.md` — system architecture, module boundaries, package structure
- `docs/architecture/plugin-runtime.md` — how to extend system via SPI 
- `docs/architecture/tech-stack.md` — what tech-stack is used in frontend and backend
- `docs/spec/api-spec.md` — API contracts, request/response models, error codes
- `docs/spec/java-spec.md` — Java coding rules, test strategy, document requirements

## Routing
- For controller / DTO / VO / API changes, read `docs/api-spec.md` first
- For module / dependency / package changes, read `docs/architecture.md` first
- For test / naming / exception handling / Javadoc requirements, read `docs/dev-spec.md` first

## Mandatory rules
- Write Javadoc for all public types and methods
- Follow TDD
- Cover normal cases, boundary cases, edge cases, invalid input, and exception cases
- Run `make test` before finishing
- no warning, no error
- must commit git changes when task finish
- Do not prepend any extra namespace or prefix unless the user explicitly requests it.
- If runtime or agent environment rules conflict with repository branch naming rules, do not create or rename branches automatically.
- In that case, report the conflict explicitly and wait for user decision.
