import React, {useState} from 'react';
import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {z} from 'zod';
import {FormInput} from '@/components/ui/form-input';
import {Button} from '@/components/ui/button';
import {Alert, AlertDescription, AlertTitle} from '@/components/ui/alert';
import {AlertCircle, CheckCircle2, Loader2} from 'lucide-react';
import {AccountType, registerCustomer} from './api';
import {ApiError} from '@/lib/api';
import {OAuthButtons} from './OAuthButtons';

const registerSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string()
        .min(8, 'Password must be at least 8 characters')
        .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
        .regex(/[0-9]/, 'Password must contain at least one number'),
    confirmPassword: z.string(),
    accountType: z.nativeEnum(AccountType),
}).refine(data => data.password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
});

type RegisterFormData = z.infer<typeof registerSchema>;

export const RegisterForm: React.FC = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const {register, handleSubmit, formState: {errors}, setError: setFieldError, watch} = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            accountType: AccountType.COMPANY,
        },
    });

    const watchedAccountType = watch('accountType');

    const onSubmit = async (data: RegisterFormData) => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);

        try {
            const result = await registerCustomer(data);
            setSuccess(true);
            console.log('Registration successful:', result);
            // TODO: Redirect to onboarding flow
        } catch (err) {
            if (err instanceof ApiError) {
                setError(err.message);

                if (err.fields) {
                    err.fields.forEach(fieldError => {
                        setFieldError(fieldError.field as keyof RegisterFormData, {
                            type: 'manual',
                            message: fieldError.message,
                        });
                    });
                }
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
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Create Account</h2>

                {error && (
                    <div className="mb-4">
                        <Alert variant="destructive">
                            <AlertCircle className="h-4 w-4" />
                            <AlertDescription>{error}</AlertDescription>
                        </Alert>
                    </div>
                )}

                {success && (
                    <div className="mb-4">
                        <Alert variant="success">
                            <CheckCircle2 className="h-4 w-4" />
                            <AlertTitle>Account created successfully!</AlertTitle>
                            <AlertDescription>Welcome to Upkeep!</AlertDescription>
                        </Alert>
                    </div>
                )}

                <OAuthButtons accountType={watchedAccountType} className="mb-6"/>

                <div className="relative mb-6">
                    <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-300"/>
                    </div>
                    <div className="relative flex justify-center text-sm">
                        <span className="bg-white px-2 text-gray-500">or register with email</span>
                    </div>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <FormInput
                        label="Email"
                        type="email"
                        {...register('email')}
                        error={errors.email?.message}
                        placeholder="you@example.com"
                    />

                    <FormInput
                        label="Password"
                        type="password"
                        {...register('password')}
                        error={errors.password?.message}
                        placeholder="••••••••"
                    />

                    <FormInput
                        label="Confirm Password"
                        type="password"
                        {...register('confirmPassword')}
                        error={errors.confirmPassword?.message}
                        placeholder="••••••••"
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Account Type
                        </label>
                        <div className="space-y-2">
                            <label className="flex items-center">
                                <input
                                    type="radio"
                                    {...register('accountType')}
                                    value={AccountType.COMPANY}
                                    className="mr-2"
                                />
                                <span>Company (I want to sponsor maintainers)</span>
                            </label>
                            <label className="flex items-center">
                                <input
                                    type="radio"
                                    {...register('accountType')}
                                    value={AccountType.MAINTAINER}
                                    className="mr-2"
                                />
                                <span>Maintainer (I maintain open-source packages)</span>
                            </label>
                            <label className="flex items-center">
                                <input
                                    type="radio"
                                    {...register('accountType')}
                                    value={AccountType.BOTH}
                                    className="mr-2"
                                />
                                <span>Both</span>
                            </label>
                        </div>
                        {errors.accountType && (
                            <p className="mt-1 text-sm text-red-600">{errors.accountType.message}</p>
                        )}
                    </div>

                    <Button type="submit" className="w-full" disabled={isLoading}>
                        {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Create Account
                    </Button>
                </form>

                <div className="mt-4 text-center text-sm text-gray-600">
                    Already have an account?{' '}
                    <a href="/login" className="text-blue-600 hover:underline">
                        Log in
                    </a>
                </div>
            </div>
        </div>
    );
};
