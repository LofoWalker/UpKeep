# Story 2.3: Invite User to Company

Status: ready-for-dev

## Story

As a **company Owner**,
I want to invite users to join my company workspace,
so that my team can collaborate on funding allocations.

## Acceptance Criteria

1. **Given** I am an Owner in a company  
   **When** I go to Settings > Team and click "Invite Member"  
   **And** I enter an email address and select a role (Owner or Member)  
   **And** I submit the invitation  
   **Then** an invitation is created with status PENDING  
   **And** an email is sent to the invitee with a unique invite link  
   **And** the invitation appears in the pending invitations list

2. **Given** I invite an email that already has a pending invitation  
   **When** I submit  
   **Then** I see an error: "An invitation is already pending for this email"

3. **Given** I am a Member (not Owner)  
   **When** I try to access the invite feature  
   **Then** I see an error or the feature is hidden

## Tasks / Subtasks

- [ ] Task 1: Create Invitation domain model (AC: #1)
  - [ ] 1.1: Create `Invitation` entity
  - [ ] 1.2: Create `InvitationStatus` enum
  - [ ] 1.3: Create `InvitationToken` value object

- [ ] Task 2: Create invitation use case (AC: #1, #2, #3)
  - [ ] 2.1: Create `InviteUserToCompanyUseCase` port
  - [ ] 2.2: Implement invitation logic with role check
  - [ ] 2.3: Create `InvitationRepository` port

- [ ] Task 3: Create infrastructure (AC: #1, #2)
  - [ ] 3.1: Implement `InvitationJpaRepository`
  - [ ] 3.2: Create database migration
  - [ ] 3.3: Create invitation email template
  - [ ] 3.4: Create REST endpoint

- [ ] Task 4: Create frontend (AC: #1, #2, #3)
  - [ ] 4.1: Create Team settings page
  - [ ] 4.2: Create invite dialog
  - [ ] 4.3: Create pending invitations list
  - [ ] 4.4: Add role-based UI hiding

## Dev Notes

### Domain Model

```java
// Invitation Entity
package com.upkeep.domain.model.invitation;

public class Invitation {
    private final InvitationId id;
    private final CompanyId companyId;
    private final Email email;
    private final Role role;
    private final InvitationToken token;
    private InvitationStatus status;
    private final UserId invitedBy;
    private final Instant createdAt;
    private final Instant expiresAt;

    public static Invitation create(
        CompanyId companyId,
        Email email,
        Role role,
        UserId invitedBy
    ) {
        return new Invitation(
            InvitationId.generate(),
            companyId,
            email,
            role,
            InvitationToken.generate(),
            InvitationStatus.PENDING,
            invitedBy,
            Instant.now(),
            Instant.now().plus(Duration.ofDays(7))
        );
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void accept() {
        if (isExpired()) {
            throw new DomainRuleException("Invitation has expired");
        }
        if (status != InvitationStatus.PENDING) {
            throw new DomainRuleException("Invitation is no longer valid");
        }
        this.status = InvitationStatus.ACCEPTED;
    }
}

public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED
}

public record InvitationToken(String value) {
    public static InvitationToken generate() {
        return new InvitationToken(UUID.randomUUID().toString());
    }
}
```

### Use Case

```java
package com.upkeep.application.port.in;

public interface InviteUserToCompanyUseCase {
    InviteResult execute(InviteCommand command);

    record InviteCommand(
        String companyId,
        String actorUserId,
        String email,
        Role role
    ) {}

    record InviteResult(String invitationId, String email) {}
}

// Implementation
@ApplicationScoped
public class InviteUserToCompanyUseCaseImpl implements InviteUserToCompanyUseCase {

    @Override
    @Transactional
    public InviteResult execute(InviteCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        UserId actorId = UserId.from(command.actorUserId());

        // Verify actor is Owner
        Membership actorMembership = membershipRepository
            .findByUserAndCompany(actorId, companyId)
            .orElseThrow(() -> new ForbiddenException("Not a member"));

        if (actorMembership.getRole() != Role.OWNER) {
            throw new ForbiddenException("Only Owners can invite members");
        }

        Email email = new Email(command.email());

        // Check for existing pending invitation
        if (invitationRepository.existsPendingByEmailAndCompany(email, companyId)) {
            throw new ConflictException("An invitation is already pending for this email");
        }

        // Check if user is already a member
        if (membershipRepository.existsByEmailAndCompany(email, companyId)) {
            throw new ConflictException("This user is already a member");
        }

        // Create invitation
        Invitation invitation = Invitation.create(companyId, email, command.role(), actorId);
        invitationRepository.save(invitation);

        // Send email
        emailService.sendInvitation(invitation);

        return new InviteResult(invitation.getId().toString(), email.value());
    }
}
```

### Database Schema

```sql
-- V6__create_invitations_table.sql
CREATE TABLE invitations (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invited_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_invitations__company_id ON invitations(company_id);
CREATE INDEX idx_invitations__email ON invitations(email);
CREATE INDEX idx_invitations__token ON invitations(token);
```

### REST Endpoint

```java
@Path("/api/companies/{companyId}/invitations")
@Authenticated
public class InvitationResource {

    @POST
    public Response inviteUser(
        @PathParam("companyId") String companyId,
        @Valid InviteRequest request,
        @Context SecurityContext ctx
    ) {
        String userId = ctx.getUserPrincipal().getName();
        
        InviteResult result = inviteUserUseCase.execute(
            new InviteCommand(companyId, userId, request.email(), request.role())
        );

        return Response.status(201)
            .entity(ApiResponse.success(result))
            .build();
    }

    @GET
    public Response listInvitations(@PathParam("companyId") String companyId) {
        List<InvitationDto> invitations = listInvitationsUseCase.execute(companyId);
        return Response.ok(ApiResponse.success(invitations)).build();
    }

    @DELETE
    @Path("/{invitationId}")
    public Response cancelInvitation(
        @PathParam("companyId") String companyId,
        @PathParam("invitationId") String invitationId,
        @Context SecurityContext ctx
    ) {
        // Cancel pending invitation
        cancelInvitationUseCase.execute(invitationId, ctx.getUserPrincipal().getName());
        return Response.ok(ApiResponse.success("Invitation cancelled")).build();
    }
}
```

### Email Template

```html
<!-- Invitation Email -->
<h1>You're invited to join {{companyName}} on Upkeep</h1>

<p>{{inviterName}} has invited you to join {{companyName}} as a {{role}}.</p>

<p>Click the link below to accept the invitation:</p>

<a href="{{inviteUrl}}">Accept Invitation</a>

<p>This invitation expires in 7 days.</p>
```

### Frontend Implementation

```tsx
// apps/web/src/features/company/components/InviteDialog.tsx
export function InviteDialog({ open, onOpenChange }: DialogProps) {
  const { currentCompany } = useCompanyContext()
  const queryClient = useQueryClient()

  const { mutate, isPending } = useMutation({
    mutationFn: (data: InviteData) => 
      api.post(`/companies/${currentCompany.id}/invitations`, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['invitations'])
      onOpenChange(false)
      toast.success('Invitation sent!')
    },
    onError: (error) => {
      toast.error(error.message)
    },
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Invite Team Member</DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit((data) => mutate(data))}>
          <div className="space-y-4">
            <div>
              <Label>Email Address</Label>
              <Input type="email" {...register('email')} />
            </div>
            
            <div>
              <Label>Role</Label>
              <Select {...register('role')}>
                <SelectTrigger>
                  <SelectValue placeholder="Select role" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MEMBER">Member</SelectItem>
                  <SelectItem value="OWNER">Owner</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button type="submit" disabled={isPending}>
              {isPending ? 'Sending...' : 'Send Invitation'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
```

### Team Settings Page

```tsx
// apps/web/src/features/company/pages/TeamSettingsPage.tsx
export function TeamSettingsPage() {
  const { currentCompany, userRole } = useCompanyContext()
  const [inviteOpen, setInviteOpen] = useState(false)

  const { data: members } = useQuery({
    queryKey: ['members', currentCompany.id],
    queryFn: () => api.get(`/companies/${currentCompany.id}/members`),
  })

  const { data: invitations } = useQuery({
    queryKey: ['invitations', currentCompany.id],
    queryFn: () => api.get(`/companies/${currentCompany.id}/invitations`),
  })

  const isOwner = userRole === 'OWNER'

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Team Members</h2>
        {isOwner && (
          <Button onClick={() => setInviteOpen(true)}>
            <PlusIcon className="mr-2 h-4 w-4" />
            Invite Member
          </Button>
        )}
      </div>

      {/* Members List */}
      <MembersList members={members} isOwner={isOwner} />

      {/* Pending Invitations */}
      {invitations?.length > 0 && (
        <>
          <h3 className="text-md font-medium">Pending Invitations</h3>
          <InvitationsList invitations={invitations} isOwner={isOwner} />
        </>
      )}

      <InviteDialog open={inviteOpen} onOpenChange={setInviteOpen} />
    </div>
  )
}
```

### Dependencies on Previous Stories

- Story 2.1: Company and Membership entities
- Story 2.2: Dashboard shell for settings navigation

### References

- [Source: architecture.md#Authentication-Security] - Authorization patterns
- [Source: epics.md#Story-2.3] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

