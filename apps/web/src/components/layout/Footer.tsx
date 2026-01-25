import { Link } from "react-router-dom";
import { Logo } from "./Logo";

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="border-t bg-background">
      <div className="mx-auto max-w-7xl px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div className="col-span-1 md:col-span-2">
            <Logo className="text-xl" linkTo="/" />
            <p className="mt-4 text-sm text-muted-foreground max-w-md">
              Open-source fund allocation platform for npm package maintainers.
              Support the developers behind your dependencies.
            </p>
          </div>

          {/* Links */}
          <div>
            <h3 className="font-semibold text-foreground mb-4">Product</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li>
                <Link to="/features" className="hover:text-foreground transition-colors">
                  Features
                </Link>
              </li>
              <li>
                <Link to="/pricing" className="hover:text-foreground transition-colors">
                  Pricing
                </Link>
              </li>
              <li>
                <Link to="/docs" className="hover:text-foreground transition-colors">
                  Documentation
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="font-semibold text-foreground mb-4">Company</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li>
                <Link to="/about" className="hover:text-foreground transition-colors">
                  About
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="hover:text-foreground transition-colors">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link to="/terms" className="hover:text-foreground transition-colors">
                  Terms of Service
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-8 border-t">
          <p className="text-sm text-muted-foreground text-center">
            © {currentYear} Upkeep. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}
