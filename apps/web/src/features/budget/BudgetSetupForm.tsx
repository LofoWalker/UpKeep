import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { setBudget, SetBudgetRequest } from './api';
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
import { useToast } from '@/hooks/use-toast';

interface BudgetSetupFormProps {
  companyId: string;
  onSuccess?: () => void;
}

export function BudgetSetupForm({ companyId, onSuccess }: BudgetSetupFormProps) {
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('EUR');
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { mutate, isPending } = useMutation({
    mutationFn: (data: SetBudgetRequest) => setBudget(companyId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budget', companyId] });
      toast({
        title: 'Success',
        description: 'Budget set successfully!',
      });
      setAmount('');
      onSuccess?.();
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to set budget',
        variant: 'destructive',
      });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const amountValue = parseFloat(amount);
    if (isNaN(amountValue) || amountValue <= 0) {
      toast({
        title: 'Invalid amount',
        description: 'Please enter a valid amount greater than 0',
        variant: 'destructive',
      });
      return;
    }

    if (amountValue < 1) {
      toast({
        title: 'Invalid amount',
        description: 'Budget must be at least 1.00',
        variant: 'destructive',
      });
      return;
    }

    const amountCents = Math.round(amountValue * 100);
    mutate({ amountCents, currency });
  };

  return (
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
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="currency">Currency</Label>
          <Select value={currency} onValueChange={setCurrency}>
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
      <Button type="submit" disabled={isPending || !amount}>
        {isPending ? 'Saving...' : 'Set Budget'}
      </Button>
    </form>
  );
}
