import { cn, formatCurrency } from '@/lib/utils';

interface BudgetBarProps {
  totalCents: number;
  allocatedCents: number;
  currency: string;
}

export function BudgetBar({ totalCents, allocatedCents, currency }: BudgetBarProps) {
  const remainingCents = totalCents - allocatedCents;
  const percentage = totalCents > 0 ? (allocatedCents / totalCents) * 100 : 0;

  return (
    <div className="space-y-2">
      <div className="flex justify-between text-sm">
        <span className="text-muted-foreground">Budget Usage</span>
        <span className="font-medium">
          {formatCurrency(allocatedCents, currency)} / {formatCurrency(totalCents, currency)}
        </span>
      </div>

      <div className="h-3 bg-muted rounded-full overflow-hidden">
        <div
          className={cn(
            "h-full rounded-full transition-all",
            percentage > 90 ? "bg-warning" : "bg-primary"
          )}
          style={{ width: `${Math.min(percentage, 100)}%` }}
        />
      </div>

      <div className="flex justify-between text-xs text-muted-foreground">
        <span>{percentage.toFixed(0)}% allocated</span>
        <span>{formatCurrency(remainingCents, currency)} remaining</span>
      </div>
    </div>
  );
}
