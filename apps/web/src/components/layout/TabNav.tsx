import {Link, useLocation} from "react-router-dom";
import {cn} from "@/lib/utils";

export interface TabItem {
  id: string;
  label: string;
  href: string;
  icon?: React.ComponentType<{ className?: string }>;
}

interface TabNavProps {
  tabs: TabItem[];
  activeTab?: string;
}

export function TabNav({ tabs, activeTab }: TabNavProps) {
  const location = useLocation();

  const getIsActive = (tab: TabItem) => {
    if (activeTab) {
      return activeTab === tab.id;
    }
    return location.pathname === tab.href;
  };

  return (
    <nav className="flex space-x-6 lg:space-x-8 overflow-x-auto">
      {tabs.map((tab) => {
        const isActive = getIsActive(tab);
        return (
          <Link
            key={tab.id}
            to={tab.href}
            className={cn(
              "border-b-2 px-1 py-4 text-sm font-medium whitespace-nowrap transition-colors",
              isActive
                ? "border-primary text-primary"
                : "border-transparent text-muted-foreground hover:border-muted hover:text-foreground"
            )}
          >
            {tab.icon && <tab.icon className="mr-2 h-4 w-4 inline-block" />}
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
