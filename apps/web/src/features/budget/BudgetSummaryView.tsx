import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getBudgetSummary } from './api';
import { BudgetBar } from '@/components/common/BudgetBar';
import { BudgetSetupForm } from './BudgetSetupForm';
import { BudgetEditForm } from './BudgetEditForm';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { LoadingSpinner } from '@/components/common';
import { Pencil } from 'lucide-react';

interface BudgetSummaryViewProps {
  companyId: string;
}

export function BudgetSummaryView({ companyId }: BudgetSummaryViewProps) {
  const [isEditing, setIsEditing] = useState(false);

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
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Monthly Budget</CardTitle>
            <CardDescription>
              Track your open-source sponsorship budget allocation
            </CardDescription>
          </div>
          {!isEditing && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => setIsEditing(true)}
            >
              <Pencil className="h-4 w-4 mr-2" />
              Edit Budget
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {isEditing ? (
          <BudgetEditForm
            companyId={companyId}
            currentAmountCents={budget.totalCents}
            currentCurrency={budget.currency}
            onSuccess={() => setIsEditing(false)}
            onCancel={() => setIsEditing(false)}
          />
        ) : (
          <BudgetBar
            totalCents={budget.totalCents}
            allocatedCents={budget.allocatedCents}
            currency={budget.currency}
          />
        )}
      </CardContent>
    </Card>
  );
}
