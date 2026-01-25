# Story 7.1: Connect Payout Method

Status: ready-for-dev

## Story

As a **maintainer**, I want to connect my payout method, so that I can receive funds.

## Acceptance Criteria

1. **Given** I am verified maintainer in Settings > Payouts, **Then** I see prompt to connect Stripe.

2. **When** I click "Connect with Stripe", **Then** I'm redirected to Stripe Connect onboarding, after completion account is linked.

3. **Given** I have connected Stripe, **Then** I see account status and can disconnect.

## Tasks

- [ ] Create `MaintainerPayoutMethod` entity (userId, stripeAccountId, status)
- [ ] Create `ConnectPayoutMethodUseCase`
- [ ] Create `StripeConnectAdapter`
- [ ] Create Stripe Connect flow in UI

## Dev Notes

### Domain Model
```java
public class MaintainerPayoutMethod {
    private final UserId userId;
    private final String stripeAccountId;
    private PayoutMethodStatus status; // PENDING, ACTIVE, DISABLED
    private final Instant connectedAt;
}
```

### Stripe Connect Flow
1. User clicks "Connect with Stripe"
2. Backend creates Stripe Connect account link
3. User completes onboarding on Stripe
4. Stripe webhook notifies account ready
5. Backend updates status to ACTIVE

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Create Page Object Model for payout settings (`e2e/pages/payout-settings.ts`)
- [ ] Test: "Connect with Stripe" button initiates OAuth flow
- [ ] Test: Payout method status displays correctly (PENDING, ACTIVE)
- [ ] Test: Connected account shows Stripe account info

**Test file location:** `apps/web/e2e/payout-settings.spec.ts`

**Note:** Mock Stripe API for E2E tests.

### References
- [Source: architecture.md#Additional-Requirements] - Stripe Connect
- [Source: epics.md#Story-7.1]
- FR31: Connect payout method
- NFR8: No raw payment details stored

## Dev Agent Record
### Agent Model Used
_To be filled_

