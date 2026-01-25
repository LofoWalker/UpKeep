import { ChevronDown, Check } from "lucide-react";
import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui";

export interface Company {
  id: string;
  name: string;
}

interface WorkspaceSwitcherProps {
  currentCompany: Company | null;
  companies: Company[];
  onCompanyChange?: (company: Company) => void;
}

export function WorkspaceSwitcher({
  currentCompany,
  companies,
  onCompanyChange,
}: WorkspaceSwitcherProps) {
  if (!currentCompany) return null;

  if (companies.length <= 1) {
    return (
      <span className="text-sm font-medium text-foreground">
        {currentCompany.name}
      </span>
    );
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="gap-2">
          {currentCompany.name}
          <ChevronDown className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-56">
        {companies.map((company) => (
          <DropdownMenuItem
            key={company.id}
            onClick={() => onCompanyChange?.(company)}
            className="flex items-center justify-between"
          >
            {company.name}
            {company.id === currentCompany.id && (
              <Check className="ml-auto h-4 w-4" />
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
