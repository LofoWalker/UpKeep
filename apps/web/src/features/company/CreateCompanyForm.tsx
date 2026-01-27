import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useCompany} from './CompanyContext';
import {Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Label} from '@/components/ui';
import {ApiError} from '@/lib/api';

function generateSlug(name: string): string {
    return name
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-|-$/g, '');
}

export function CreateCompanyForm() {
    const navigate = useNavigate();
    const { createCompany } = useCompany();
    const [name, setName] = useState('');
    const [slug, setSlug] = useState('');
    const [slugEdited, setSlugEdited] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        if (!slugEdited && name) {
            setSlug(generateSlug(name));
        }
    }, [name, slugEdited]);

    const handleSlugChange = (value: string) => {
        setSlugEdited(true);
        setSlug(value.toLowerCase().replace(/[^a-z0-9-]/g, ''));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setFieldErrors({});
        setIsLoading(true);

        try {
            await createCompany({ name, slug: slug || undefined });
            navigate('/dashboard');
        } catch (err) {
            if (err instanceof ApiError) {
                setError(err.message);
                if (err.fields) {
                    const errors: Record<string, string> = {};
                    err.fields.forEach(f => {
                        errors[f.field] = f.message;
                    });
                    setFieldErrors(errors);
                }
            } else {
                setError('An unexpected error occurred');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Card className="w-full max-w-md mx-auto">
            <CardHeader>
                <CardTitle>Create Company Workspace</CardTitle>
                <CardDescription>
                    Set up your company workspace to start managing open-source funding.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                    {error && (
                        <div className="p-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded-md">
                            {error}
                        </div>
                    )}

                    <div className="space-y-2">
                        <Label htmlFor="name">Company Name</Label>
                        <Input
                            id="name"
                            type="text"
                            placeholder="Acme Inc"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                            minLength={2}
                            maxLength={100}
                            disabled={isLoading}
                        />
                        {fieldErrors.name && (
                            <p className="text-sm text-red-600">{fieldErrors.name}</p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="slug">Company URL</Label>
                        <div className="flex items-center">
                            <span className="text-sm text-muted-foreground mr-2">upkeep.dev/</span>
                            <Input
                                id="slug"
                                type="text"
                                placeholder="acme-inc"
                                value={slug}
                                onChange={(e) => handleSlugChange(e.target.value)}
                                minLength={2}
                                maxLength={50}
                                disabled={isLoading}
                                className="flex-1"
                            />
                        </div>
                        {fieldErrors.slug && (
                            <p className="text-sm text-red-600">{fieldErrors.slug}</p>
                        )}
                        <p className="text-xs text-muted-foreground">
                            This will be your company's unique URL. Only lowercase letters, numbers, and hyphens.
                        </p>
                    </div>

                    <Button type="submit" className="w-full" disabled={isLoading}>
                        {isLoading ? 'Creating...' : 'Create Workspace'}
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
}
