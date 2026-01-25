import { useState } from "react";
import { Menu, X } from "lucide-react";
import { Button } from "@/components/ui";
import { Logo } from "./Logo";
import { UserMenu } from "./UserMenu";
import { WorkspaceSwitcher, Company } from "./WorkspaceSwitcher";
import { cn } from "@/lib/utils";

interface NavbarProps {
  currentCompany?: Company | null;
  companies?: Company[];
  onCompanyChange?: (company: Company) => void;
}

export function Navbar({
  currentCompany = null,
  companies = [],
  onCompanyChange,
}: NavbarProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <header className="border-b bg-background">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Left side: Logo and Workspace */}
          <div className="flex items-center gap-4">
            <Logo className="text-xl" />
            {currentCompany && (
              <>
                <span className="hidden sm:block text-muted-foreground">/</span>
                <div className="hidden sm:block">
                  <WorkspaceSwitcher
                    currentCompany={currentCompany}
                    companies={companies}
                    onCompanyChange={onCompanyChange}
                  />
                </div>
              </>
            )}
          </div>

          {/* Right side: User menu */}
          <div className="flex items-center gap-4">
            <div className="hidden sm:block">
              <UserMenu />
            </div>

            {/* Mobile menu button */}
            <Button
              variant="ghost"
              size="icon"
              className="sm:hidden"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              {mobileMenuOpen ? (
                <X className="h-5 w-5" />
              ) : (
                <Menu className="h-5 w-5" />
              )}
            </Button>
          </div>
        </div>

        {/* Mobile menu */}
        <div
          className={cn(
            "sm:hidden border-t py-4 space-y-4",
            mobileMenuOpen ? "block" : "hidden"
          )}
        >
          {currentCompany && (
            <div className="px-2">
              <WorkspaceSwitcher
                currentCompany={currentCompany}
                companies={companies}
                onCompanyChange={onCompanyChange}
              />
            </div>
          )}
          <div className="px-2">
            <UserMenu />
          </div>
        </div>
      </div>
    </header>
  );
}
