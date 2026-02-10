# Story 6.1: Maintainer Account Creation

Status: ready-for-dev

## Story

As a **visitor**, I want to create a maintainer account, so that I can claim packages and receive payouts.

## Acceptance Criteria

1. **Given** I select "I'm a maintainer" on registration, **When** I complete registration, **Then** account is created with type=MAINTAINER and I'm redirected to maintainer onboarding.

2. **Given** I have existing company account, **When** I create maintainer with same email, **Then** accounts are linked and I can switch between views.

## Tasks

- [ ] Update `User.accountType` to support COMPANY, MAINTAINER, BOTH
- [ ] Create `RegisterMaintainerUseCase`
- [ ] Add maintainer option to registration form
- [ ] Create maintainer routing

## Dev Notes

### User Entity Update
```java
public enum AccountType {
    COMPANY,
    MAINTAINER,
    BOTH
}

// When linking accounts
public void addMaintainerRole() {
    if (this.accountType == AccountType.COMPANY) {
        this.accountType = AccountType.BOTH;
    }
}
```


### References
- [Source: epics.md#Story-6.1]
- FR21: Maintainer account creation

## Dev Agent Record
### Agent Model Used
_To be filled_

