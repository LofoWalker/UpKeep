import { cn } from "@/lib/utils";
import { Link } from "react-router-dom";

interface LogoProps {
  className?: string;
  linkTo?: string;
}

export function Logo({ className, linkTo = "/" }: LogoProps) {
  const logoContent = (
    <span className={cn("font-bold text-primary", className)}>Upkeep</span>
  );

  if (linkTo) {
    return (
      <Link to={linkTo} className="hover:opacity-80 transition-opacity">
        {logoContent}
      </Link>
    );
  }

  return logoContent;
}
