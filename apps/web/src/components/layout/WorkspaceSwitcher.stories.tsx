import type {Meta, StoryObj} from "@storybook/react";
import {MemoryRouter} from "react-router-dom";
import {WorkspaceSwitcher} from "./WorkspaceSwitcher";

const meta: Meta<typeof WorkspaceSwitcher> = {
  title: "Layout/WorkspaceSwitcher",
  component: WorkspaceSwitcher,
  tags: ["autodocs"],
  decorators: [
    (Story) => (
      <MemoryRouter>
        <div className="p-4">
          <Story />
        </div>
      </MemoryRouter>
    ),
  ],
};

export default meta;
type Story = StoryObj<typeof WorkspaceSwitcher>;

const singleCompany = { id: "1", name: "Acme Corp" };

const multipleCompanies = [
  { id: "1", name: "Acme Corp" },
  { id: "2", name: "Tech Startup" },
  { id: "3", name: "Open Source Foundation" },
];

export const SingleWorkspace: Story = {
  args: {
    currentCompany: singleCompany,
    companies: [singleCompany],
  },
};

export const MultipleWorkspaces: Story = {
  args: {
    currentCompany: multipleCompanies[0],
    companies: multipleCompanies,
    onCompanyChange: (company) => alert(`Switching to ${company.name}`),
  },
};

export const NoWorkspace: Story = {
  args: {
    currentCompany: null,
    companies: [],
  },
};
