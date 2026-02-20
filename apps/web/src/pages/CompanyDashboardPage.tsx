import {useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {DashboardLayout} from '@/components/layout';
import {Role, useCompany} from '@/features/company';
import {useAuth} from '@/features/auth';
import {Badge, Button, Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui';
import {ArrowRight, Building2, Check, DollarSign, Package, Settings, Users} from 'lucide-react';

const tabs = [
    { id: 'overview', label: 'Overview', href: '/dashboard' },
    { id: 'budget', label: 'Budget', href: '/dashboard/budget' },
    { id: 'packages', label: 'Packages', href: '/dashboard/packages' },
    { id: 'allocations', label: 'Allocations', href: '/dashboard/allocations' },
    { id: 'settings', label: 'Settings', href: '/dashboard/settings' },
];

export function CompanyDashboardPage() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const { companies, currentCompany, dashboard, isLoading, hasFetchedCompanies, setCurrentCompany } = useCompany();

    useEffect(() => {
        if (hasFetchedCompanies && companies.length === 0 && user?.accountType === 'COMPANY') {
            navigate('/company/create');
        }
    }, [hasFetchedCompanies, companies, user, navigate]);

    if (isLoading) {
        return (
            <DashboardLayout>
                <div className="flex items-center justify-center h-64">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
            </DashboardLayout>
        );
    }

    if (!currentCompany || !dashboard) {
        return (
            <DashboardLayout>
                <div className="text-center py-12">
                    <Building2 className="mx-auto h-12 w-12 text-muted-foreground" />
                    <h3 className="mt-4 text-lg font-semibold">No company selected</h3>
                    <p className="mt-2 text-muted-foreground">
                        Create or select a company to get started.
                    </p>
                    <Button onClick={() => navigate('/company/create')} className="mt-4">
                        Create Company
                    </Button>
                </div>
            </DashboardLayout>
        );
    }

    const handleCompanyChange = (company: { id: string; name: string }) => {
        const fullCompany = companies.find(c => c.id === company.id);
        if (fullCompany) {
            setCurrentCompany(fullCompany);
        }
    };

    return (
        <DashboardLayout
            tabs={tabs}
            activeTab="overview"
            currentCompany={currentCompany}
            companies={companies}
            onCompanyChange={handleCompanyChange}
        >
            <div className="space-y-6">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-bold">{dashboard.name}</h1>
                        <p className="text-muted-foreground">
                            Welcome to your company dashboard
                            {dashboard.userRole === Role.OWNER && (
                                <Badge variant="secondary" className="ml-2">Owner</Badge>
                            )}
                        </p>
                    </div>
                    {dashboard.userRole === Role.OWNER && (
                        <Button variant="outline" onClick={() => navigate('/dashboard/settings')}>
                            <Settings className="mr-2 h-4 w-4" />
                            Settings
                        </Button>
                    )}
                </div>

                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Team Members</CardTitle>
                            <Users className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{dashboard.stats.totalMembers}</div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Monthly Budget</CardTitle>
                            <DollarSign className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">
                                {dashboard.stats.hasBudget ? '$--' : 'Not set'}
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Packages</CardTitle>
                            <Package className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">
                                {dashboard.stats.hasPackages ? '--' : '0'}
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Allocations</CardTitle>
                            <Building2 className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">
                                {dashboard.stats.hasAllocations ? '--' : '0'}
                            </div>
                        </CardContent>
                    </Card>
                </div>

                {(!dashboard.stats.hasBudget || !dashboard.stats.hasPackages || !dashboard.stats.hasAllocations) && (
                    <Card>
                        <CardHeader>
                            <CardTitle>Get Started</CardTitle>
                            <CardDescription>
                                Complete these steps to start funding open-source maintainers.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className={`flex items-center justify-between p-4 border rounded-lg ${dashboard.stats.hasBudget ? 'bg-muted/50' : ''}`}>
                                <div className="flex items-center gap-4">
                                    {dashboard.stats.hasBudget ? (
                                        <div className="flex items-center justify-center w-8 h-8 rounded-full bg-green-600 text-white text-sm font-medium">
                                            <Check className="h-4 w-4" />
                                        </div>
                                    ) : (
                                        <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-primary-foreground text-sm font-medium">
                                            1
                                        </div>
                                    )}
                                    <div>
                                        <p className={`font-medium ${dashboard.stats.hasBudget ? 'line-through text-muted-foreground' : ''}`}>
                                            Set your monthly budget
                                        </p>
                                        <p className="text-sm text-muted-foreground">
                                            Define how much you want to allocate to open-source each month
                                        </p>
                                    </div>
                                </div>
                                {!dashboard.stats.hasBudget && (
                                    <Button variant="outline" size="sm" onClick={() => navigate('/dashboard/budget')}>
                                        Set Budget
                                        <ArrowRight className="ml-2 h-4 w-4" />
                                    </Button>
                                )}
                            </div>

                            <div className={`flex items-center justify-between p-4 border rounded-lg ${dashboard.stats.hasPackages ? 'bg-muted/50' : ''}`}>
                                <div className="flex items-center gap-4">
                                    {dashboard.stats.hasPackages ? (
                                        <div className="flex items-center justify-center w-8 h-8 rounded-full bg-green-600 text-white text-sm font-medium">
                                            <Check className="h-4 w-4" />
                                        </div>
                                    ) : (
                                        <div className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-medium ${dashboard.stats.hasBudget ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'}`}>
                                            2
                                        </div>
                                    )}
                                    <div>
                                        <p className={`font-medium ${dashboard.stats.hasPackages ? 'line-through text-muted-foreground' : ''}`}>
                                            Import your packages
                                        </p>
                                        <p className="text-sm text-muted-foreground">
                                            Upload your package-lock.json or yarn.lock file
                                        </p>
                                    </div>
                                </div>
                                {!dashboard.stats.hasPackages && (
                                    <Button variant="outline" size="sm" onClick={() => navigate('/dashboard/packages')} disabled={!dashboard.stats.hasBudget}>
                                        Import
                                        <ArrowRight className="ml-2 h-4 w-4" />
                                    </Button>
                                )}
                            </div>

                            <div className="flex items-center justify-between p-4 border rounded-lg">
                                <div className="flex items-center gap-4">
                                    <div className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-medium ${dashboard.stats.hasPackages && dashboard.stats.hasBudget ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'}`}>
                                        3
                                    </div>
                                    <div>
                                        <p className="font-medium">Create your first allocation</p>
                                        <p className="text-sm text-muted-foreground">
                                            Distribute your budget across your dependencies
                                        </p>
                                    </div>
                                </div>
                                <Button variant="outline" size="sm" disabled={!dashboard.stats.hasBudget || !dashboard.stats.hasPackages}>
                                    Allocate
                                    <ArrowRight className="ml-2 h-4 w-4" />
                                </Button>
                            </div>
                        </CardContent>
                    </Card>
                )}
            </div>
        </DashboardLayout>
    );
}
