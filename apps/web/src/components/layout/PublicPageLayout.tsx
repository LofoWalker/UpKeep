import { PublicHeader } from "./PublicHeader";
import { Footer } from "./Footer";

interface PublicPageLayoutProps {
  children: React.ReactNode;
  hero?: React.ReactNode;
}

export function PublicPageLayout({ children, hero }: PublicPageLayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      <PublicHeader />

      {hero && (
        <div className="bg-gradient-to-b from-primary/5 to-background">{hero}</div>
      )}

      <main className="flex-1">{children}</main>

      <Footer />
    </div>
  );
}
