import type {Meta, StoryObj} from "@storybook/react";
import {PageError} from "./PageError";

const meta: Meta<typeof PageError> = {
  title: "Layout/PageError",
  component: PageError,
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof PageError>;

export const Default: Story = {};

export const WithCustomMessage: Story = {
  args: {
    title: "Page Not Found",
    message: "The page you're looking for doesn't exist or has been moved.",
  },
};

export const WithRetryButton: Story = {
  args: {
    title: "Failed to Load Data",
    message: "We couldn't load your data. Please try again.",
    retry: () => alert("Retrying..."),
  },
};

export const NetworkError: Story = {
  args: {
    title: "Connection Error",
    message:
      "Unable to connect to the server. Please check your internet connection and try again.",
    retry: () => alert("Retrying..."),
  },
};
