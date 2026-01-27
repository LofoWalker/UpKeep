import type {Meta, StoryObj} from "@storybook/react";
import {MemoryRouter} from "react-router-dom";
import {ProgressStepper} from "./ProgressStepper";

const meta: Meta<typeof ProgressStepper> = {
  title: "Layout/ProgressStepper",
  component: ProgressStepper,
  tags: ["autodocs"],
  decorators: [
    (Story) => (
      <MemoryRouter>
        <div className="max-w-2xl mx-auto p-8">
          <Story />
        </div>
      </MemoryRouter>
    ),
  ],
};

export default meta;
type Story = StoryObj<typeof ProgressStepper>;

const defaultSteps = [
  { id: "workspace", label: "Workspace" },
  { id: "budget", label: "Budget" },
  { id: "dependencies", label: "Dependencies" },
  { id: "allocate", label: "Allocate" },
];

export const FirstStep: Story = {
  args: {
    steps: defaultSteps,
    currentStep: 0,
  },
};

export const MiddleStep: Story = {
  args: {
    steps: defaultSteps,
    currentStep: 2,
  },
};

export const LastStep: Story = {
  args: {
    steps: defaultSteps,
    currentStep: 3,
  },
};

export const Completed: Story = {
  args: {
    steps: defaultSteps,
    currentStep: 4,
  },
};

export const TwoSteps: Story = {
  args: {
    steps: [
      { id: "profile", label: "Profile" },
      { id: "complete", label: "Complete" },
    ],
    currentStep: 0,
  },
};
