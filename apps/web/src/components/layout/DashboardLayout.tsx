import {Navbar} from "./Navbar";
import {TabItem, TabNav} from "./TabNav";
import {Company} from "./WorkspaceSwitcher";

interface DashboardLayoutProps {
  children: React.ReactNode;
  tabs?: TabItem[];
  activeTab?: string;
  currentCompany?: Company | null;
  companies?: Company[];
  onCompanyChange?: (company: Company) => void;
}

export function DashboardLayout({
  children,
  tabs,
  activeTab,
  currentCompany,
  companies,
  onCompanyChange,
}: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-muted/30">
      <Navbar
        currentCompany={currentCompany}
        companies={companies}
        onCompanyChange={onCompanyChange}
      />

      {tabs && tabs.length > 0 && (
        <div className="border-b bg-background">
          <div className="mx-auto max-w-7xl px-4">
            <TabNav tabs={tabs} activeTab={activeTab} />
          </div>
        </div>
      )}

      <main className="mx-auto max-w-7xl px-4 py-6">{children}</main>
    </div>
  );
}
