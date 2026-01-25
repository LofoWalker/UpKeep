import { cn } from "@/lib/utils";
import { Check } from "lucide-react";

export interface Step {
  id: string;
  label: string;
  description?: string;
}

interface ProgressStepperProps {
  steps: Step[];
  currentStep: number;
}

export function ProgressStepper({ steps, currentStep }: ProgressStepperProps) {
  return (
    <nav aria-label="Progress">
      <ol className="flex items-center justify-between">
        {steps.map((step, index) => (
          <li key={step.id} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={cn(
                  "flex h-8 w-8 items-center justify-center rounded-full border-2 text-sm font-medium transition-colors",
                  index < currentStep &&
                    "border-primary bg-primary text-primary-foreground",
                  index === currentStep && "border-primary text-primary",
                  index > currentStep && "border-muted text-muted-foreground"
                )}
              >
                {index < currentStep ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <span>{index + 1}</span>
                )}
              </div>
              <span
                className={cn(
                  "mt-2 text-xs font-medium hidden sm:block",
                  index <= currentStep
                    ? "text-foreground"
                    : "text-muted-foreground"
                )}
              >
                {step.label}
              </span>
            </div>
            {index < steps.length - 1 && (
              <div
                className={cn(
                  "mx-2 sm:mx-4 h-0.5 w-8 sm:w-16 lg:w-24",
                  index < currentStep ? "bg-primary" : "bg-muted"
                )}
              />
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
}
