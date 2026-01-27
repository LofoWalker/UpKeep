import {useEffect, useState} from 'react';
import {useLocation, useNavigate, useSearchParams} from 'react-router-dom';
import {PublicPageLayout} from '@/components/layout';
import {useAuth} from '@/features/auth';
import {acceptInvitation, getInvitationDetails, InvitationDetails, InvitationStatus, Role} from '@/features/company';
import {Badge, Button, Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui';
import {AlertTriangle, Building2, CheckCircle2, Clock, XCircle} from 'lucide-react';
import {ApiError} from '@/lib/api';

export function AcceptInvitationPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const { isAuthenticated, isLoading: authLoading } = useAuth();

    const [invitation, setInvitation] = useState<InvitationDetails | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isAccepting, setIsAccepting] = useState(false);
    const [accepted, setAccepted] = useState(false);

    useEffect(() => {
        if (!token) {
            setError('Invalid invitation link');
            setIsLoading(false);
            return;
        }

        async function loadInvitation() {
            try {
                const data = await getInvitationDetails(token!);
                setInvitation(data);
            } catch (err) {
                if (err instanceof ApiError) {
                    setError(err.message);
                } else {
                    setError('Failed to load invitation');
                }
            } finally {
                setIsLoading(false);
            }
        }

        loadInvitation();
    }, [token]);

    const handleAccept = async () => {
        if (!token) return;

        if (!isAuthenticated) {
            navigate('/login', { state: { from: location } });
            return;
        }

        setIsAccepting(true);
        try {
            await acceptInvitation(token);
            setAccepted(true);
            setTimeout(() => {
                navigate('/dashboard');
            }, 2000);
        } catch (err) {
            if (err instanceof ApiError) {
                setError(err.message);
            } else {
                setError('Failed to accept invitation');
            }
        } finally {
            setIsAccepting(false);
        }
    };

    const handleDecline = () => {
        navigate('/');
    };

    if (isLoading || authLoading) {
        return (
            <PublicPageLayout>
                <div className="flex items-center justify-center min-h-[50vh]">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
            </PublicPageLayout>
        );
    }

    if (error && !invitation) {
        return (
            <PublicPageLayout>
                <div className="max-w-md mx-auto mt-16">
                    <Card>
                        <CardContent className="pt-6 text-center">
                            <XCircle className="mx-auto h-12 w-12 text-destructive" />
                            <h2 className="mt-4 text-lg font-semibold">Invalid Invitation</h2>
                            <p className="mt-2 text-muted-foreground">{error}</p>
                            <Button onClick={() => navigate('/')} className="mt-4">
                                Go to Home
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </PublicPageLayout>
        );
    }

    if (accepted) {
        return (
            <PublicPageLayout>
                <div className="max-w-md mx-auto mt-16">
                    <Card>
                        <CardContent className="pt-6 text-center">
                            <CheckCircle2 className="mx-auto h-12 w-12 text-green-500" />
                            <h2 className="mt-4 text-lg font-semibold">Welcome to the team!</h2>
                            <p className="mt-2 text-muted-foreground">
                                You've joined {invitation?.companyName}. Redirecting to dashboard...
                            </p>
                        </CardContent>
                    </Card>
                </div>
            </PublicPageLayout>
        );
    }

    if (!invitation) return null;

    const isExpired = invitation.isExpired || invitation.status === InvitationStatus.EXPIRED;
    const isAlreadyAccepted = invitation.status === InvitationStatus.ACCEPTED;

    if (isExpired) {
        return (
            <PublicPageLayout>
                <div className="max-w-md mx-auto mt-16">
                    <Card>
                        <CardContent className="pt-6 text-center">
                            <Clock className="mx-auto h-12 w-12 text-muted-foreground" />
                            <h2 className="mt-4 text-lg font-semibold">Invitation Expired</h2>
                            <p className="mt-2 text-muted-foreground">
                                This invitation has expired. Please ask the company owner to send a new invitation.
                            </p>
                            <Button onClick={() => navigate('/')} className="mt-4">
                                Go to Home
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </PublicPageLayout>
        );
    }

    if (isAlreadyAccepted) {
        return (
            <PublicPageLayout>
                <div className="max-w-md mx-auto mt-16">
                    <Card>
                        <CardContent className="pt-6 text-center">
                            <CheckCircle2 className="mx-auto h-12 w-12 text-green-500" />
                            <h2 className="mt-4 text-lg font-semibold">Already Accepted</h2>
                            <p className="mt-2 text-muted-foreground">
                                This invitation has already been accepted.
                            </p>
                            <Button onClick={() => navigate('/dashboard')} className="mt-4">
                                Go to Dashboard
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </PublicPageLayout>
        );
    }

    return (
        <PublicPageLayout>
            <div className="max-w-md mx-auto mt-16">
                <Card>
                    <CardHeader className="text-center">
                        <div className="mx-auto w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mb-4">
                            <Building2 className="h-6 w-6 text-primary" />
                        </div>
                        <CardTitle>You've been invited!</CardTitle>
                        <CardDescription>
                            Join {invitation.companyName} on Upkeep
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="bg-muted/50 rounded-lg p-4 space-y-2">
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Company</span>
                                <span className="font-medium">{invitation.companyName}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Role</span>
                                <Badge variant={invitation.role === Role.OWNER ? 'default' : 'secondary'}>
                                    {invitation.role}
                                </Badge>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Expires</span>
                                <span className="text-sm">
                                    {new Date(invitation.expiresAt).toLocaleDateString()}
                                </span>
                            </div>
                        </div>

                        {error && (
                            <div className="flex items-center gap-2 p-3 bg-destructive/10 text-destructive rounded-lg">
                                <AlertTriangle className="h-4 w-4" />
                                <span className="text-sm">{error}</span>
                            </div>
                        )}

                        {!isAuthenticated && (
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm text-blue-700">
                                You'll need to sign in or create an account to accept this invitation.
                            </div>
                        )}

                        <div className="flex gap-3">
                            <Button variant="outline" onClick={handleDecline} className="flex-1">
                                Decline
                            </Button>
                            <Button onClick={handleAccept} disabled={isAccepting} className="flex-1">
                                {isAccepting ? 'Accepting...' : isAuthenticated ? 'Accept' : 'Sign in to Accept'}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </PublicPageLayout>
    );
}
