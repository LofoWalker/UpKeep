import {AlertCircle} from "lucide-react";
import {Button} from "@/components/ui";

interface PageErrorProps {
  title?: string;
  message?: string;
  retry?: () => void;
}

export function PageError({
  title = "Something went wrong",
  message = "An unexpected error occurred",
  retry,
}: PageErrorProps) {
  return (
    <div className="flex min-h-[400px] flex-col items-center justify-center gap-4">
      <AlertCircle className="h-12 w-12 text-destructive" />
      <h2 className="text-lg font-semibold">{title}</h2>
      <p className="text-muted-foreground text-center max-w-md">{message}</p>
      {retry && (
        <Button onClick={retry} variant="outline">
          Try again
        </Button>
      )}
    </div>
  );
}
