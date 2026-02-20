import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useInfiniteQuery } from '@tanstack/react-query';
import { DashboardLayout } from '@/components/layout';
import { useCompany } from '@/features/company';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/components/ui';
import { useToast } from '@/hooks/use-toast';
import { LoadingSpinner } from '@/components/common';
import { Package as PackageIcon, Search, Upload } from 'lucide-react';
import { listPackages, importFromLockfile } from '@/features/packages/api';
import { FileDropzone } from '@/features/packages/FileDropzone';
import { PastePackagesDialog } from '@/features/packages/PastePackagesDialog';
import { PackageCard } from '@/features/packages/PackageCard';

const PAGE_SIZE = 50;

const tabs = [
    { id: 'overview', label: 'Overview', href: '/dashboard' },
    { id: 'budget', label: 'Budget', href: '/dashboard/budget' },
    { id: 'packages', label: 'Packages', href: '/dashboard/packages' },
    { id: 'allocations', label: 'Allocations', href: '/dashboard/allocations' },
    { id: 'settings', label: 'Settings', href: '/dashboard/settings' },
];

export function PackagesPage() {
    const navigate = useNavigate();
    const { companies, currentCompany, setCurrentCompany, refreshDashboard } = useCompany();
    const { toast } = useToast();
    const [search, setSearch] = useState('');
    const [debouncedSearch, setDebouncedSearch] = useState('');
    const debounceRef = useRef<ReturnType<typeof setTimeout>>();
    const loadMoreRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => {
            setDebouncedSearch(search);
        }, 300);
        return () => clearTimeout(debounceRef.current);
    }, [search]);

    const {
        data,
        isLoading,
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
        refetch,
    } = useInfiniteQuery({
        queryKey: ['packages', currentCompany?.id, debouncedSearch],
        queryFn: ({ pageParam = 0 }) =>
            listPackages(currentCompany!.id, pageParam, PAGE_SIZE, debouncedSearch || undefined),
        getNextPageParam: (lastPage) => {
            const loadedCount = (lastPage.page + 1) * lastPage.size;
            return loadedCount < lastPage.totalCount ? lastPage.page + 1 : undefined;
        },
        initialPageParam: 0,
        enabled: !!currentCompany,
    });

    // Infinite scroll via IntersectionObserver
    useEffect(() => {
        const element = loadMoreRef.current;
        if (!element) return;

        const observer = new IntersectionObserver(
            (entries) => {
                if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
                    fetchNextPage();
                }
            },
            { threshold: 0.1 }
        );

        observer.observe(element);
        return () => observer.disconnect();
    }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

    const importMutation = useMutation({
        mutationFn: ({ content, filename }: { content: string; filename: string }) =>
            importFromLockfile(currentCompany!.id, { fileContent: content, filename }),
        onSuccess: async (result) => {
            toast({
                title: 'Import complete',
                description: `Imported ${result.importedCount} packages` +
                    (result.skippedCount > 0 ? `, ${result.skippedCount} already existed` : ''),
            });
            await refetch();
            await refreshDashboard();
        },
        onError: (error) => {
            toast({
                title: 'Import failed',
                description: error instanceof Error ? error.message : 'Failed to parse lockfile',
                variant: 'destructive',
            });
        },
    });

    const handleFileAccepted = useCallback((file: File) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const content = e.target?.result as string;
            importMutation.mutate({ content, filename: file.name });
        };
        reader.onerror = () => {
            toast({
                title: 'File read error',
                description: 'Failed to read the file. Please try again.',
                variant: 'destructive',
            });
        };
        reader.readAsText(file);
    }, [importMutation, toast]);

    const handleListImportSuccess = useCallback(async () => {
        await refetch();
        await refreshDashboard();
    }, [refetch, refreshDashboard]);

    if (!currentCompany) {
        return (
            <DashboardLayout>
                <div className="text-center py-12">
                    <p className="text-muted-foreground">Please select a company first.</p>
                    <Button onClick={() => navigate('/dashboard')} className="mt-4">
                        Go to Dashboard
                    </Button>
                </div>
            </DashboardLayout>
        );
    }

    const handleCompanyChange = (company: { id: string; name: string }) => {
        const fullCompany = companies.find(c => c.id === company.id);
        if (fullCompany) {
            setCurrentCompany(fullCompany);
        }
    };

    const allPackages = data?.pages.flatMap(p => p.packages) ?? [];
    const totalCount = data?.pages[0]?.totalCount ?? 0;

    return (
        <DashboardLayout
            tabs={tabs}
            activeTab="packages"
            currentCompany={currentCompany}
            companies={companies}
            onCompanyChange={handleCompanyChange}
        >
            <div className="space-y-6">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-bold">Packages</h1>
                        <p className="text-muted-foreground">
                            Manage your company&apos;s tracked dependencies
                        </p>
                    </div>
                    <div className="flex gap-2">
                        <PastePackagesDialog
                            companyId={currentCompany.id}
                            onSuccess={handleListImportSuccess}
                        />
                    </div>
                </div>

                {totalCount === 0 && !isLoading && !debouncedSearch ? (
                    <Card>
                        <CardHeader className="text-center">
                            <PackageIcon className="mx-auto h-12 w-12 text-muted-foreground" />
                            <CardTitle className="mt-4">No packages imported</CardTitle>
                            <CardDescription>
                                Import your dependencies to start tracking and allocating funds.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <FileDropzone
                                onFileAccepted={handleFileAccepted}
                                isLoading={importMutation.isPending}
                            />
                            <div className="text-center text-sm text-muted-foreground">
                                or use the &quot;Paste package list&quot; button above
                            </div>
                        </CardContent>
                    </Card>
                ) : (
                    <>
                        <div className="flex items-center gap-4">
                            <div className="relative flex-1 max-w-md">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Search packages..."
                                    value={search}
                                    onChange={(e) => setSearch(e.target.value)}
                                    className="pl-9"
                                />
                            </div>
                            <span className="text-sm text-muted-foreground">
                                {totalCount} package{totalCount !== 1 ? 's' : ''}
                            </span>
                        </div>

                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Upload className="h-5 w-5" />
                                    Import more packages
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <FileDropzone
                                    onFileAccepted={handleFileAccepted}
                                    isLoading={importMutation.isPending}
                                />
                            </CardContent>
                        </Card>

                        {isLoading ? (
                            <div className="flex justify-center py-8">
                                <LoadingSpinner />
                            </div>
                        ) : (
                            <div className="space-y-2">
                                {allPackages.map((pkg) => (
                                    <PackageCard
                                        key={pkg.id}
                                        name={pkg.name}
                                        registry={pkg.registry}
                                        importedAt={pkg.importedAt}
                                    />
                                ))}

                                <div ref={loadMoreRef} className="h-4" />

                                {isFetchingNextPage && (
                                    <div className="flex justify-center py-4">
                                        <LoadingSpinner />
                                    </div>
                                )}

                                {allPackages.length === 0 && debouncedSearch && (
                                    <div className="text-center py-8 text-muted-foreground">
                                        No packages matching &quot;{debouncedSearch}&quot;
                                    </div>
                                )}
                            </div>
                        )}
                    </>
                )}
            </div>
        </DashboardLayout>
    );
}

