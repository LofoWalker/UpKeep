import { DashboardLayout } from '@/components/layout';
import { useCompany } from '@/features/company';
import { BudgetSummaryView } from '@/features/budget';
import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';

const tabs = [
  { id: 'overview', label: 'Overview', href: '/dashboard' },
  { id: 'budget', label: 'Budget', href: '/dashboard/budget' },
  { id: 'packages', label: 'Packages', href: '/dashboard/packages' },
  { id: 'allocations', label: 'Allocations', href: '/dashboard/allocations' },
  { id: 'settings', label: 'Settings', href: '/dashboard/settings' },
];

export function BudgetPage() {
  const navigate = useNavigate();
  const { companies, currentCompany, setCurrentCompany } = useCompany();

  if (!currentCompany) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <p className="text-muted-foreground">Please select a company first.</p>
          <Button onClick={() => navigate('/dashboard')} className="mt-4">
            Go to Dashboard
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
      activeTab="budget"
      currentCompany={currentCompany}
      companies={companies}
      onCompanyChange={handleCompanyChange}
    >
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate('/dashboard')}
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <div>
            <h1 className="text-2xl font-bold">Budget Management</h1>
            <p className="text-muted-foreground">
              Set and manage your monthly open-source sponsorship budget
            </p>
          </div>
        </div>

        <BudgetSummaryView companyId={currentCompany.id} />
      </div>
    </DashboardLayout>
  );
}
