import {useCallback, useEffect, useState} from 'react';
import {DashboardLayout} from '@/components/layout';
import {getCompanyMembers, inviteUser, MemberInfo, Role, updateMemberRole, useCompany} from '@/features/company';
import {
    Badge,
    Button,
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
    Input,
    Label,
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
    useToast
} from '@/components/ui';
import {MoreVertical, Shield, User, UserPlus} from 'lucide-react';
import {ApiError} from '@/lib/api';

const tabs = [
    { id: 'overview', label: 'Overview', href: '/dashboard' },
    { id: 'budget', label: 'Budget', href: '/dashboard/budget' },
    { id: 'packages', label: 'Packages', href: '/dashboard/packages' },
    { id: 'allocations', label: 'Allocations', href: '/dashboard/allocations' },
    { id: 'settings', label: 'Settings', href: '/dashboard/settings' },
];

export function TeamSettingsPage() {
    const { toast } = useToast();
    const { companies, currentCompany, dashboard, setCurrentCompany } = useCompany();
    const [members, setMembers] = useState<MemberInfo[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isInviteOpen, setIsInviteOpen] = useState(false);
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviteRole, setInviteRole] = useState<Role>(Role.MEMBER);
    const [isInviting, setIsInviting] = useState(false);

    const loadMembers = useCallback(async () => {
        if (!currentCompany) return;
        setIsLoading(true);
        try {
            const data = await getCompanyMembers(currentCompany.id);
            setMembers(data);
        } catch (err) {
            toast({
                variant: 'destructive',
                title: 'Error',
                description: err instanceof Error ? err.message : 'Failed to load members',
            });
        } finally {
            setIsLoading(false);
        }
    }, [currentCompany, toast]);

    useEffect(() => {
        loadMembers();
    }, [loadMembers]);

    const handleInvite = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!currentCompany) return;

        setIsInviting(true);
        try {
            await inviteUser(currentCompany.id, { email: inviteEmail, role: inviteRole });
            toast({
                title: 'Invitation sent',
                description: `An invitation has been sent to ${inviteEmail}`,
            });
            setIsInviteOpen(false);
            setInviteEmail('');
            setInviteRole(Role.MEMBER);
        } catch (err) {
            toast({
                variant: 'destructive',
                title: 'Failed to send invitation',
                description: err instanceof ApiError ? err.message : 'An error occurred',
            });
        } finally {
            setIsInviting(false);
        }
    };

    const handleRoleChange = async (membershipId: string, newRole: Role) => {
        if (!currentCompany) return;

        try {
            await updateMemberRole(currentCompany.id, membershipId, { role: newRole });
            toast({
                title: 'Role updated',
                description: 'Member role has been updated successfully',
            });
            loadMembers();
        } catch (err) {
            toast({
                variant: 'destructive',
                title: 'Failed to update role',
                description: err instanceof ApiError ? err.message : 'An error occurred',
            });
        }
    };

    const handleCompanyChange = (company: { id: string; name: string }) => {
        const fullCompany = companies.find(c => c.id === company.id);
        if (fullCompany) {
            setCurrentCompany(fullCompany);
        }
    };

    const isOwner = dashboard?.userRole === Role.OWNER;

    return (
        <DashboardLayout
            tabs={tabs}
            activeTab="settings"
            currentCompany={currentCompany}
            companies={companies}
            onCompanyChange={handleCompanyChange}
        >
            <div className="space-y-6">
                <div>
                    <h1 className="text-2xl font-bold">Team Settings</h1>
                    <p className="text-muted-foreground">
                        Manage your team members and their roles
                    </p>
                </div>

                <Card>
                    <CardHeader>
                        <div className="flex items-center justify-between">
                            <div>
                                <CardTitle>Team Members</CardTitle>
                                <CardDescription>
                                    {members.length} member{members.length !== 1 ? 's' : ''} in this workspace
                                </CardDescription>
                            </div>
                            {isOwner && (
                                <Dialog open={isInviteOpen} onOpenChange={setIsInviteOpen}>
                                    <DialogTrigger asChild>
                                        <Button>
                                            <UserPlus className="mr-2 h-4 w-4" />
                                            Invite Member
                                        </Button>
                                    </DialogTrigger>
                                    <DialogContent>
                                        <form onSubmit={handleInvite}>
                                            <DialogHeader>
                                                <DialogTitle>Invite Team Member</DialogTitle>
                                                <DialogDescription>
                                                    Send an invitation to join your company workspace.
                                                </DialogDescription>
                                            </DialogHeader>
                                            <div className="space-y-4 py-4">
                                                <div className="space-y-2">
                                                    <Label htmlFor="email">Email address</Label>
                                                    <Input
                                                        id="email"
                                                        type="email"
                                                        placeholder="colleague@example.com"
                                                        value={inviteEmail}
                                                        onChange={(e) => setInviteEmail(e.target.value)}
                                                        required
                                                    />
                                                </div>
                                                <div className="space-y-2">
                                                    <Label htmlFor="role">Role</Label>
                                                    <Select value={inviteRole} onValueChange={(v) => setInviteRole(v as Role)}>
                                                        <SelectTrigger>
                                                            <SelectValue />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            <SelectItem value={Role.MEMBER}>Member</SelectItem>
                                                            <SelectItem value={Role.OWNER}>Owner</SelectItem>
                                                        </SelectContent>
                                                    </Select>
                                                    <p className="text-xs text-muted-foreground">
                                                        Owners can manage team members and settings. Members can view and create allocations.
                                                    </p>
                                                </div>
                                            </div>
                                            <DialogFooter>
                                                <Button type="button" variant="outline" onClick={() => setIsInviteOpen(false)}>
                                                    Cancel
                                                </Button>
                                                <Button type="submit" disabled={isInviting}>
                                                    {isInviting ? 'Sending...' : 'Send Invitation'}
                                                </Button>
                                            </DialogFooter>
                                        </form>
                                    </DialogContent>
                                </Dialog>
                            )}
                        </div>
                    </CardHeader>
                    <CardContent>
                        {isLoading ? (
                            <div className="flex items-center justify-center h-32">
                                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                            </div>
                        ) : (
                            <div className="divide-y">
                                {members.map((member) => (
                                    <div key={member.membershipId} className="flex items-center justify-between py-4">
                                        <div className="flex items-center gap-3">
                                            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-muted">
                                                <User className="h-5 w-5 text-muted-foreground" />
                                            </div>
                                            <div>
                                                <div className="flex items-center gap-2">
                                                    <span className="font-medium">{member.email}</span>
                                                    <Badge variant={member.role === Role.OWNER ? 'default' : 'secondary'}>
                                                        {member.role === Role.OWNER ? (
                                                            <><Shield className="mr-1 h-3 w-3" /> Owner</>
                                                        ) : (
                                                            'Member'
                                                        )}
                                                    </Badge>
                                                </div>
                                                <p className="text-sm text-muted-foreground">
                                                    Joined {new Date(member.joinedAt).toLocaleDateString()}
                                                </p>
                                            </div>
                                        </div>
                                        {isOwner && (
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" size="sm">
                                                        <MoreVertical className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    {member.role === Role.MEMBER ? (
                                                        <DropdownMenuItem onClick={() => handleRoleChange(member.membershipId, Role.OWNER)}>
                                                            <Shield className="mr-2 h-4 w-4" />
                                                            Make Owner
                                                        </DropdownMenuItem>
                                                    ) : (
                                                        <DropdownMenuItem onClick={() => handleRoleChange(member.membershipId, Role.MEMBER)}>
                                                            <User className="mr-2 h-4 w-4" />
                                                            Make Member
                                                        </DropdownMenuItem>
                                                    )}
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </DashboardLayout>
    );
}
