# Story 2.5: Manage Team Roles

Status: ready-for-dev

## Story

As a **company Owner**,
I want to view and change member roles,
so that I can control access levels in my company.

## Acceptance Criteria

1. **Given** I am an Owner  
   **When** I go to Settings > Team  
   **Then** I see a list of all members with:
   - Name, email, role, joined date
   - Action menu for each member

2. **Given** I click "Change Role" on a Member  
   **When** I select "Owner"  
   **Then** their role is updated to Owner  
   **And** they gain Owner permissions immediately

3. **Given** I try to demote myself (the only Owner)  
   **When** I attempt the action  
   **Then** I see an error: "Cannot remove the last Owner"

4. **Given** I am a Member  
   **When** I view the team page  
   **Then** I can see members but cannot change roles

## Tasks / Subtasks

- [ ] Task 1: Create role management use case (AC: #2, #3)
  - [ ] 1.1: Create `UpdateMemberRoleUseCase` port
  - [ ] 1.2: Implement role change with last owner protection
  - [ ] 1.3: Create `ListCompanyMembersUseCase`

- [ ] Task 2: Create REST endpoints (AC: #1, #2)
  - [ ] 2.1: Create GET `/api/companies/{id}/members`
  - [ ] 2.2: Create PATCH `/api/companies/{id}/members/{memberId}`
  - [ ] 2.3: Create DELETE endpoint for removing members

- [ ] Task 3: Create frontend (AC: #1, #2, #3, #4)
  - [ ] 3.1: Create members list component
  - [ ] 3.2: Create role change dialog
  - [ ] 3.3: Add role-based action visibility

## Dev Notes

### Use Case Implementation

```java
@ApplicationScoped
public class UpdateMemberRoleUseCaseImpl implements UpdateMemberRoleUseCase {

    @Override
    @Transactional
    public void execute(UpdateRoleCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        UserId actorId = UserId.from(command.actorUserId());
        MembershipId targetId = MembershipId.from(command.membershipId());

        // Verify actor is Owner
        Membership actor = membershipRepository.findByUserAndCompany(actorId, companyId)
            .orElseThrow(() -> new ForbiddenException("Not a member"));
        
        if (actor.getRole() != Role.OWNER) {
            throw new ForbiddenException("Only Owners can change roles");
        }

        Membership target = membershipRepository.findById(targetId)
            .orElseThrow(() -> new NotFoundException("Membership", targetId.toString()));

        // Prevent demoting last owner
        if (target.getRole() == Role.OWNER && command.newRole() == Role.MEMBER) {
            long ownerCount = membershipRepository.countByCompanyAndRole(companyId, Role.OWNER);
            if (ownerCount <= 1) {
                throw new DomainRuleException("Cannot remove the last Owner");
            }
        }

        target.setRole(command.newRole());
        membershipRepository.save(target);
    }
}
```

### Database Query

```java
// MembershipRepository
long countByCompanyAndRole(CompanyId companyId, Role role);

// SQL
SELECT COUNT(*) FROM memberships WHERE company_id = ? AND role = ?
```

### REST Endpoint

```java
@Path("/api/companies/{companyId}/members")
@Authenticated
public class MemberResource {

    @GET
    public Response listMembers(@PathParam("companyId") String companyId) {
        List<MemberDto> members = listMembersUseCase.execute(companyId);
        return Response.ok(ApiResponse.success(members)).build();
    }

    @PATCH
    @Path("/{membershipId}")
    public Response updateRole(
        @PathParam("companyId") String companyId,
        @PathParam("membershipId") String membershipId,
        @Valid UpdateRoleRequest request,
        @Context SecurityContext ctx
    ) {
        updateMemberRoleUseCase.execute(new UpdateRoleCommand(
            companyId,
            ctx.getUserPrincipal().getName(),
            membershipId,
            request.role()
        ));
        return Response.ok(ApiResponse.success("Role updated")).build();
    }

    @DELETE
    @Path("/{membershipId}")
    public Response removeMember(
        @PathParam("companyId") String companyId,
        @PathParam("membershipId") String membershipId,
        @Context SecurityContext ctx
    ) {
        removeMemberUseCase.execute(companyId, membershipId, ctx.getUserPrincipal().getName());
        return Response.ok(ApiResponse.success("Member removed")).build();
    }
}
```

### Frontend Members List

```tsx
export function MembersList({ members, isOwner }: MembersListProps) {
  const [roleDialogOpen, setRoleDialogOpen] = useState(false)
  const [selectedMember, setSelectedMember] = useState<Member | null>(null)

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Member</TableHead>
          <TableHead>Role</TableHead>
          <TableHead>Joined</TableHead>
          {isOwner && <TableHead className="w-[50px]"></TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {members.map((member) => (
          <TableRow key={member.id}>
            <TableCell>
              <div className="flex items-center gap-3">
                <Avatar>
                  <AvatarFallback>{member.email[0].toUpperCase()}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-medium">{member.name || member.email}</p>
                  <p className="text-sm text-muted-foreground">{member.email}</p>
                </div>
              </div>
            </TableCell>
            <TableCell>
              <Badge variant={member.role === 'OWNER' ? 'default' : 'secondary'}>
                {member.role}
              </Badge>
            </TableCell>
            <TableCell>{formatDate(member.joinedAt)}</TableCell>
            {isOwner && (
              <TableCell>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon">
                      <MoreHorizontalIcon className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => {
                      setSelectedMember(member)
                      setRoleDialogOpen(true)
                    }}>
                      Change Role
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem className="text-error">
                      Remove from team
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
```

### Dependencies on Previous Stories

- Story 2.1: Membership entity
- Story 2.3: Team settings page

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Owner can change member's role
- [ ] Test: Owner cannot demote themselves if they are the last owner
- [ ] Test: Owner can remove a member from the team
- [ ] Test: Member cannot change roles (feature hidden or error)

**Test file location:** `apps/web/e2e/team-roles.spec.ts`

### References

- [Source: architecture.md#Authentication-Security] - RBAC
- [Source: epics.md#Story-2.5] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used
_To be filled by dev agent_

### Completion Notes List
_To be filled during implementation_

### Change Log
_To be filled during implementation_

### File List
_To be filled after implementation_

