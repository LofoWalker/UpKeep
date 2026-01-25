import type { Meta, StoryObj } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";
import { TabNav } from "./TabNav";
import {
  LayoutDashboard,
  Package,
  DollarSign,
  Settings,
} from "lucide-react";

const meta: Meta<typeof TabNav> = {
  title: "Layout/TabNav",
  component: TabNav,
  tags: ["autodocs"],
  decorators: [
    (Story) => (
      <MemoryRouter initialEntries={["/dashboard"]}>
        <div className="border-b bg-background">
          <div className="max-w-4xl mx-auto px-4">
            <Story />
          </div>
        </div>
      </MemoryRouter>
    ),
  ],
};

export default meta;
type Story = StoryObj<typeof TabNav>;

const dashboardTabs = [
  { id: "overview", label: "Overview", href: "/dashboard" },
  { id: "packages", label: "Packages", href: "/dashboard/packages" },
  { id: "allocations", label: "Allocations", href: "/dashboard/allocations" },
  { id: "settings", label: "Settings", href: "/dashboard/settings" },
];

const tabsWithIcons = [
  { id: "overview", label: "Overview", href: "/dashboard", icon: LayoutDashboard },
  { id: "packages", label: "Packages", href: "/dashboard/packages", icon: Package },
  { id: "allocations", label: "Allocations", href: "/dashboard/allocations", icon: DollarSign },
  { id: "settings", label: "Settings", href: "/dashboard/settings", icon: Settings },
];

export const Default: Story = {
  args: {
    tabs: dashboardTabs,
    activeTab: "overview",
  },
};

export const SecondTabActive: Story = {
  args: {
    tabs: dashboardTabs,
    activeTab: "packages",
  },
};

export const WithIcons: Story = {
  args: {
    tabs: tabsWithIcons,
    activeTab: "overview",
  },
};

export const TwoTabs: Story = {
  args: {
    tabs: [
      { id: "profile", label: "Profile", href: "/settings/profile" },
      { id: "security", label: "Security", href: "/settings/security" },
    ],
    activeTab: "profile",
  },
};
