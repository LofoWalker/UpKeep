import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui";
import { Logo } from "./Logo";
import { ProgressStepper, Step } from "./ProgressStepper";

interface OnboardingLayoutProps {
  children: React.ReactNode;
  currentStep: number;
  steps: Step[];
  title?: string;
}

export function OnboardingLayout({
  children,
  currentStep,
  steps,
  title,
}: OnboardingLayoutProps) {
  return (
    <div className="min-h-screen bg-muted/30 flex flex-col">
      {/* Header */}
      <header className="border-b bg-background px-4 py-3">
        <div className="mx-auto max-w-2xl flex items-center justify-center">
          <Logo className="text-2xl" />
        </div>
      </header>

      {/* Progress */}
      <div className="border-b bg-background px-4 py-4">
        <div className="mx-auto max-w-2xl">
          <ProgressStepper steps={steps} currentStep={currentStep} />
        </div>
      </div>

      {/* Content */}
      <main className="flex-1 px-4 py-8">
        <div className="mx-auto max-w-2xl">
          <Card>
            {title && (
              <CardHeader>
                <CardTitle>{title}</CardTitle>
              </CardHeader>
            )}
            <CardContent className={title ? undefined : "pt-6"}>
              {children}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
}
