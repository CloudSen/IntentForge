# API Specification Guide

## Purpose
This document explains the API design conventions used in this project.

The source of truth for all HTTP API contracts is:

- `docs/api-spec.yaml`

## Rules

- All HTTP APIs must be defined in `docs/api-spec.yaml`.
- The OpenAPI document must use `openapi: 3.1.0`.
- Request and response models must be defined in reusable schemas where appropriate.
- Every endpoint must define:
  - summary
  - operationId
  - tags
  - parameters if applicable
  - requestBody if applicable
  - responses
- Every schema field should include:
  - type
  - description
  - required constraints where applicable
  - format where applicable
  - example where helpful

## Response conventions

- Success and error responses must be explicitly documented.
- Do not rely on implicit response structures.
- `4xx` and `5xx` responses should have defined payload schemas where applicable.
- Long-running run responses should expose observable runtime-selection data when implementation choice affects execution.
- Checkpoint-based run responses should expose both the already selected route history and the currently available next actions whenever user selection can change the next step.

## Naming conventions

- Paths use lowercase letters and hyphens.
- operationId must be stable and descriptive.
- Schema names must be clear and business-oriented.
- Avoid vague names such as `Data`, `Result`, or `Info`.

## Compatibility rules

- Breaking API changes require explicit review.
- Adding optional response fields is preferred over changing existing field meanings.
- Removing fields or changing field types is considered breaking.

## Documentation requirements

- Public APIs must remain synchronized with `docs/api-spec.yaml`.
- Code implementation must not diverge from the OpenAPI contract.
- Tests should verify behavior against the documented contract where feasible.
