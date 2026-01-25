import React, {useState, useEffect} from 'react';
import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {z} from 'zod';
import {Link, useLocation, useNavigate, useSearchParams} from 'react-router-dom';
import {FormInput} from '@/components/ui/form-input';
import {Button} from '@/components/ui/button';
import {Alert, AlertDescription} from '@/components/ui/alert';
import {AlertCircle, Loader2} from 'lucide-react';
import {ApiError} from '@/lib/api.ts';
import {useAuth} from "@/features/auth/useAuth.ts";
import {OAuthButtons} from './OAuthButtons';

const loginSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginForm: React.FC = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const {login} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const from = (location.state as { from?: Location })?.from?.pathname || '/dashboard';

    useEffect(() => {
        const oauthError = searchParams.get('error');
        const oauthMessage = searchParams.get('message');
        if (oauthError) {
            setError(oauthMessage || 'OAuth authentication failed. Please try again.');
        }
    }, [searchParams]);

    const {register, handleSubmit, formState: {errors}} = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (data: LoginFormData) => {
        setIsLoading(true);
        setError(null);

        try {
            await login(data.email, data.password);
            navigate(from, {replace: true});
        } catch (err) {
            if (err instanceof ApiError) {
                setError(err.message);
            } else {
                setError('An unexpected error occurred. Please try again.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md mx-auto">
            <div className="bg-white shadow-md rounded-lg p-8">
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Sign In</h2>

                {error && (
                    <div className="mb-4">
                        <Alert variant="destructive">
                            <AlertCircle className="h-4 w-4" />
                            <AlertDescription>{error}</AlertDescription>
                        </Alert>
                    </div>
                )}

                <OAuthButtons className="mb-6"/>

                <div className="relative mb-6">
                    <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-300"/>
                    </div>
                    <div className="relative flex justify-center text-sm">
                        <span className="bg-white px-2 text-gray-500">or continue with email</span>
                    </div>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <FormInput
                        label="Email"
                        type="email"
                        {...register('email')}
                        error={errors.email?.message}
                        placeholder="you@example.com"
                        autoComplete="email"
                    />

                    <FormInput
                        label="Password"
                        type="password"
                        {...register('password')}
                        error={errors.password?.message}
                        placeholder="••••••••"
                        autoComplete="current-password"
                    />

                    <Button type="submit" className="w-full" disabled={isLoading}>
                        {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Sign In
                    </Button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-600">
                    Don't have an account?{' '}
                    <Link to="/register" className="text-blue-600 hover:text-blue-500 font-medium">
                        Create one
                    </Link>
                </p>
            </div>
        </div>
    );
};
