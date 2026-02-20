import { Badge, Card } from '@/components/ui';
import { Package as PackageIcon } from 'lucide-react';
import { formatCurrency } from '@/lib/utils';

interface PackageCardProps {
  name: string;
  registry: string;
  importedAt: string;
  allocationCents?: number;
  claimStatus?: 'claimed' | 'unclaimed';
  currency?: string;
}

export function PackageCard({
  name,
  registry,
  allocationCents,
  claimStatus = 'unclaimed',
  currency,
}: PackageCardProps) {
  return (
    <Card className="p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <PackageIcon className="h-8 w-8 text-muted-foreground" />
          <div>
            <p className="font-medium">{name}</p>
            <div className="flex items-center gap-2 mt-1">
              <Badge variant="outline" className="text-xs">
                {registry}
              </Badge>
              <Badge variant={claimStatus === 'claimed' ? 'default' : 'secondary'}>
                {claimStatus}
              </Badge>
            </div>
          </div>
        </div>
        {allocationCents != null && currency && (
          <p className="font-semibold">{formatCurrency(allocationCents, currency)}</p>
        )}
      </div>
    </Card>
  );
}

