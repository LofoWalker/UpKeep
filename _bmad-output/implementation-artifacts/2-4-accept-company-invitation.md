# Story 2.4: Accept Company Invitation

Status: ready-for-dev

## Story

As an **invited user**,
I want to accept an invitation to join a company,
so that I can collaborate with the team.

## Acceptance Criteria

1. **Given** I received an invitation email  
   **When** I click the invite link  
   **Then** I am taken to the invitation acceptance page showing:
   - Company name
   - Role I'm being invited as
   - Accept / Decline buttons

2. **Given** I am not logged in  
   **When** I click Accept  
   **Then** I am prompted to log in or create an account  
   **And** after authentication, the invitation is accepted

3. **Given** I am logged in  
   **When** I click Accept  
   **Then** I become a member of the company with the assigned role  
   **And** I am redirected to the company dashboard  
   **And** the invitation status changes to ACCEPTED

4. **Given** the invitation has expired  
   **When** I click the link  
   **Then** I see an error: "This invitation has expired"

## Tasks / Subtasks

- [ ] Task 1: Create accept invitation use case (AC: #2, #3, #4)
  - [ ] 1.1: Create `AcceptInvitationUseCase` port
  - [ ] 1.2: Implement acceptance logic
  - [ ] 1.3: Create `GetInvitationByTokenUseCase` for display

- [ ] Task 2: Create REST endpoints (AC: #1, #3)
  - [ ] 2.1: Create GET `/api/invitations/{token}` for details
  - [ ] 2.2: Create POST `/api/invitations/{token}/accept`
  - [ ] 2.3: Create POST `/api/invitations/{token}/decline`

- [ ] Task 3: Create frontend (AC: #1, #2, #3, #4)
  - [ ] 3.1: Create invitation page at `/invite/{token}`
  - [ ] 3.2: Handle unauthenticated flow
  - [ ] 3.3: Handle expired invitation
  - [ ] 3.4: Redirect after acceptance

## Dev Notes

### Use Case Implementation

```java
@ApplicationScoped
public class AcceptInvitationUseCaseImpl implements AcceptInvitationUseCase {

    @Override
    @Transactional
    public AcceptResult execute(AcceptCommand command) {
        InvitationToken token = new InvitationToken(command.token());
        UserId userId = UserId.from(command.userId());

        Invitation invitation = invitationRepository.findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invitation", token.value()));

        // Check expiry
        if (invitation.isExpired()) {
            throw new DomainRuleException("This invitation has expired");
        }

        // Check status
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new DomainRuleException("This invitation is no longer valid");
        }

        // Accept invitation
        invitation.accept();
        invitationRepository.save(invitation);

        // Create membership
        Membership membership = new Membership(
            MembershipId.generate(),
            userId,
            invitation.getCompanyId(),
            invitation.getRole(),
            Instant.now()
        );
        membershipRepository.save(membership);

        Company company = companyRepository.findById(invitation.getCompanyId())
            .orElseThrow();

        return new AcceptResult(company.getId().toString(), company.getName());
    }
}
```

### REST Endpoints

```java
@Path("/api/invitations")
public class InvitationPublicResource {

    @GET
    @Path("/{token}")
    public Response getInvitationDetails(@PathParam("token") String token) {
        InvitationDetails details = getInvitationByTokenUseCase.execute(token);
        return Response.ok(ApiResponse.success(details)).build();
    }

    @POST
    @Path("/{token}/accept")
    @Authenticated
    public Response acceptInvitation(
        @PathParam("token") String token,
        @Context SecurityContext ctx
    ) {
        String userId = ctx.getUserPrincipal().getName();
        AcceptResult result = acceptInvitationUseCase.execute(
            new AcceptCommand(token, userId)
        );
        return Response.ok(ApiResponse.success(result)).build();
    }

    @POST
    @Path("/{token}/decline")
    public Response declineInvitation(@PathParam("token") String token) {
        declineInvitationUseCase.execute(token);
        return Response.ok(ApiResponse.success("Invitation declined")).build();
    }
}
```

### Frontend Invitation Page

```tsx
// apps/web/src/pages/invite/[token].tsx
export function InvitationPage() {
  const { token } = useParams()
  const { isAuthenticated, user } = useAuth()
  const navigate = useNavigate()

  const { data: invitation, isLoading, error } = useQuery({
    queryKey: ['invitation', token],
    queryFn: () => api.get(`/invitations/${token}`),
  })

  const acceptMutation = useMutation({
    mutationFn: () => api.post(`/invitations/${token}/accept`),
    onSuccess: (data) => {
      toast.success(`Welcome to ${data.companyName}!`)
      navigate('/dashboard')
    },
  })

  if (isLoading) return <PageLoading />
  
  if (error?.code === 'NOT_FOUND') {
    return <InvitationNotFound />
  }

  if (invitation?.status === 'EXPIRED' || invitation?.isExpired) {
    return <InvitationExpired />
  }

  const handleAccept = () => {
    if (!isAuthenticated) {
      // Store token and redirect to login
      sessionStorage.setItem('pendingInvitation', token)
      navigate(`/login?redirect=/invite/${token}`)
      return
    }
    acceptMutation.mutate()
  }

  return (
    <PublicPageLayout>
      <div className="mx-auto max-w-md py-12">
        <Card>
          <CardHeader className="text-center">
            <CardTitle>You're Invited!</CardTitle>
            <CardDescription>
              Join {invitation.companyName} on Upkeep
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="rounded-lg bg-muted p-4 text-center">
              <p className="text-sm text-muted-foreground">You're invited as</p>
              <p className="text-lg font-semibold">{invitation.role}</p>
            </div>

            <div className="flex gap-3">
              <Button 
                variant="outline" 
                className="flex-1"
                onClick={() => declineMutation.mutate()}
              >
                Decline
              </Button>
              <Button 
                className="flex-1"
                onClick={handleAccept}
                disabled={acceptMutation.isPending}
              >
                {acceptMutation.isPending ? 'Accepting...' : 'Accept Invitation'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </PublicPageLayout>
  )
}
```

### Dependencies on Previous Stories

- Story 2.3: Invitation entity and creation

### References

- [Source: epics.md#Story-2.4] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used
_To be filled by dev agent_

### Completion Notes List
_To be filled during implementation_

### Change Log
_To be filled during implementation_

### File List
_To be filled after implementation_

