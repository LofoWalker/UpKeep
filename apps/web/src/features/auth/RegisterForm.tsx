import React, {useState} from 'react';
import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {z} from 'zod';
import {Input} from '../../components/ui/Input';
import {Button} from '../../components/ui/Button';
import {Alert} from '../../components/ui/Alert';
import {AccountType, registerCustomer} from './api';
import {ApiError} from '../../lib/api';

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

    const {register, handleSubmit, formState: {errors}, setError: setFieldError} = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            accountType: AccountType.COMPANY,
        },
    });

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
                        <Alert type="error" message={error}/>
                    </div>
                )}

                {success && (
                    <div className="mb-4">
                        <Alert type="success" message="Account created successfully!" details="Welcome to Upkeep!"/>
                    </div>
                )}

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <Input
                        label="Email"
                        type="email"
                        {...register('email')}
                        error={errors.email?.message}
                        placeholder="you@example.com"
                    />

                    <Input
                        label="Password"
                        type="password"
                        {...register('password')}
                        error={errors.password?.message}
                        placeholder="••••••••"
                    />

                    <Input
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

                    <Button type="submit" className="w-full" isLoading={isLoading}>
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
