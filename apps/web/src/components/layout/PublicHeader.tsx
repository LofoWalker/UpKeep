import { Link } from "react-router-dom";
import { Logo } from "./Logo";

export function PublicHeader() {
  return (
    <header className="border-b bg-background">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-16 items-center justify-between">
          <Logo className="text-xl" />
          <nav className="flex items-center gap-6">
            <Link
              to="/login"
              className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              Sign in
            </Link>
            <Link
              to="/register"
              className="text-sm font-medium bg-primary text-primary-foreground px-4 py-2 rounded-md hover:bg-primary/90 transition-colors"
            >
              Get Started
            </Link>
          </nav>
        </div>
      </div>
    </header>
  );
}
