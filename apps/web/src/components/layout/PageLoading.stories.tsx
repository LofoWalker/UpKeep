import type { Meta, StoryObj } from "@storybook/react";
import { PageLoading } from "./PageLoading";

const meta: Meta<typeof PageLoading> = {
  title: "Layout/PageLoading",
  component: PageLoading,
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof PageLoading>;

export const Default: Story = {};

export const WithMessage: Story = {
  args: {
    message: "Loading your dashboard...",
  },
};

export const CustomMessage: Story = {
  args: {
    message: "Fetching data from server...",
  },
};
