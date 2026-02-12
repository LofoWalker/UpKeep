# Story 7.3: Execute Payout Run

Status: ready-for-dev

## Story

As a **system operator**, I want to execute a payout run, so that maintainers receive their payments.

## Acceptance Criteria

1. **Given** distributions calculated, **When** I trigger "Execute Payouts", **Then** run status → RUNNING, for each item: if Stripe connected → transfer and PAID, if no method → HELD_UNCLAIMED, if fails → FAILED with reason.

2. **When** all processed, **Then** run status → COMPLETED, audit event recorded (FR39).

3. **Given** re-executed, **Then** only PENDING and FAILED items retried (idempotency NFR9).

## Tasks

- [ ] Create `ExecutePayoutRunUseCase`
- [ ] Create `StripePayoutAdapter` with idempotency keys
- [ ] Create audit event for payout run
- [ ] Implement idempotent execution

## Dev Notes

### Idempotency
```java
// Use combination of runId + lineItemId as Stripe idempotency key
String idempotencyKey = runId + "-" + lineItemId;

// Only process items in PENDING or FAILED status
List<PayoutLineItem> itemsToProcess = lineItems.stream()
    .filter(item -> item.getStatus() == PENDING || item.getStatus() == FAILED)
    .toList();
```

### Stripe Transfer
```java
Transfer transfer = Transfer.create(
    TransferCreateParams.builder()
        .setAmount(item.getAmountCents())
        .setCurrency("eur")
        .setDestination(maintainer.getStripeAccountId())
        .setIdempotencyKey(idempotencyKey)
        .build()
);
```


### References
- [Source: architecture.md#Communication-State-Machine-Patterns] - Payout states
- [Source: epics.md#Story-7.3]
- FR28, FR39: Execute payout with audit
- NFR9: Idempotent payout runs

## Dev Agent Record
### Agent Model Used
_To be filled_

