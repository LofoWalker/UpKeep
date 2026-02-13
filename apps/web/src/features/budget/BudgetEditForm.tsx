import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateBudget, UpdateBudgetRequest } from './api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';

interface BudgetEditFormProps {
  companyId: string;
  currentAmountCents: number;
  currentCurrency: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function BudgetEditForm({
  companyId,
  currentAmountCents,
  currentCurrency,
  onSuccess,
  onCancel
}: BudgetEditFormProps) {
  const [amount, setAmount] = useState((currentAmountCents / 100).toFixed(2));
  const [currency, setCurrency] = useState(currentCurrency);
  const [showWarning, setShowWarning] = useState(false);
  const [pendingUpdate, setPendingUpdate] = useState<UpdateBudgetRequest | null>(null);
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { mutate, isPending } = useMutation({
    mutationFn: (data: UpdateBudgetRequest) => updateBudget(companyId, data),
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ['budget', companyId] });

      if (result.isLowerThanAllocations) {
        toast({
          title: 'Budget updated with warning',
          description: `Budget is now lower than current allocations (${formatCurrency(result.currentAllocationsCents, result.currency)})`,
          variant: 'default',
        });
      } else {
        toast({
          title: 'Success',
          description: 'Budget updated successfully!',
        });
      }

      setShowWarning(false);
      setPendingUpdate(null);
      onSuccess?.();
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to update budget',
        variant: 'destructive',
      });
      setShowWarning(false);
      setPendingUpdate(null);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const amountValue = parseFloat(amount);
    if (isNaN(amountValue) || amountValue < 1) {
      toast({
        title: 'Invalid amount',
        description: 'Budget must be at least 1.00',
        variant: 'destructive',
      });
      return;
    }

    const amountCents = Math.round(amountValue * 100);
    const updateRequest = { amountCents, currency };

    if (amountCents < currentAmountCents) {
      setPendingUpdate(updateRequest);
      setShowWarning(true);
    } else {
      mutate(updateRequest);
    }
  };

  const handleConfirmUpdate = () => {
    if (pendingUpdate) {
      mutate(pendingUpdate);
    }
  };

  const handleCancelWarning = () => {
    setShowWarning(false);
    setPendingUpdate(null);
  };

  return (
    <>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="amount">Monthly Budget</Label>
            <Input
              id="amount"
              type="number"
              step="0.01"
              min="1"
              placeholder="500.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
              disabled={isPending}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="currency">Currency</Label>
            <Select value={currency} onValueChange={setCurrency} disabled={isPending}>
              <SelectTrigger id="currency">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="EUR">EUR (€)</SelectItem>
                <SelectItem value="USD">USD ($)</SelectItem>
                <SelectItem value="GBP">GBP (£)</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div className="flex gap-2 justify-end">
          {onCancel && (
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
              disabled={isPending}
            >
              Cancel
            </Button>
          )}
          <Button type="submit" disabled={isPending}>
            {isPending ? 'Updating...' : 'Update Budget'}
          </Button>
        </div>
      </form>

      <Dialog open={showWarning} onOpenChange={setShowWarning}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Budget Reduction</DialogTitle>
            <DialogDescription>
              You are reducing the budget from {formatCurrency(currentAmountCents, currentCurrency)} to {formatCurrency(pendingUpdate?.amountCents ?? 0, currency)}.
              This may affect your ability to maintain current allocations.
              Do you want to proceed?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={handleCancelWarning}>
              Cancel
            </Button>
            <Button onClick={handleConfirmUpdate}>
              Proceed
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

