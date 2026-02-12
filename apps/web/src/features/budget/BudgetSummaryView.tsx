import { useQuery } from '@tanstack/react-query';
import { getBudgetSummary } from './api';
import { BudgetBar } from '@/components/common/BudgetBar';
import { BudgetSetupForm } from './BudgetSetupForm';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/common';

interface BudgetSummaryViewProps {
  companyId: string;
}

export function BudgetSummaryView({ companyId }: BudgetSummaryViewProps) {
  const { data: budget, isLoading } = useQuery({
    queryKey: ['budget', companyId],
    queryFn: () => getBudgetSummary(companyId),
  });

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!budget?.exists) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Set Your Monthly Budget</CardTitle>
          <CardDescription>
            Define your monthly open-source sponsorship budget to start allocating funds to packages.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <BudgetSetupForm companyId={companyId} />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Monthly Budget</CardTitle>
        <CardDescription>
          Track your open-source sponsorship budget allocation
        </CardDescription>
      </CardHeader>
      <CardContent>
        <BudgetBar
          totalCents={budget.totalCents}
          allocatedCents={budget.allocatedCents}
          currency={budget.currency}
        />
      </CardContent>
    </Card>
  );
}
