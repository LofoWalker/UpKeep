import React from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '@/features/auth';

export const OnboardingPage: React.FC = () => {
    const {user} = useAuth();
    const navigate = useNavigate();

    const handleContinue = () => {
        navigate('/dashboard');
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
                <div className="text-center mb-8">
                    <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-4">
                        <svg
                            className="w-8 h-8 text-green-600"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M5 13l4 4L19 7"
                            />
                        </svg>
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900 mb-2">
                        Welcome to Upkeep!
                    </h1>
                    <p className="text-gray-600">
                        Your account has been created successfully via GitHub.
                    </p>
                </div>

                <div className="bg-gray-50 rounded-md p-4 mb-6">
                    <h2 className="text-sm font-medium text-gray-700 mb-2">Account Details</h2>
                    <p className="text-sm text-gray-600">
                        <span className="font-medium">Email:</span> {user?.email}
                    </p>
                    <p className="text-sm text-gray-600">
                        <span className="font-medium">Account Type:</span> {user?.accountType}
                    </p>
                </div>

                <div className="space-y-4">
                    <h2 className="text-lg font-semibold text-gray-900">What's Next?</h2>
                    <ul className="space-y-3 text-sm text-gray-600">
                        {user?.accountType === 'COMPANY' && (
                            <>
                                <li className="flex items-start">
                                    <span className="text-blue-600 mr-2">•</span>
                                    Create your company workspace
                                </li>
                                <li className="flex items-start">
                                    <span className="text-blue-600 mr-2">•</span>
                                    Import your npm dependencies
                                </li>
                                <li className="flex items-start">
                                    <span className="text-blue-600 mr-2">•</span>
                                    Set up your monthly sponsorship budget
                                </li>
                            </>
                        )}
                        {user?.accountType === 'MAINTAINER' && (
                            <>
                                <li className="flex items-start">
                                    <span className="text-blue-600 mr-2">•</span>
                                    Complete your maintainer profile
                                </li>
                                <li className="flex items-start">
                                    <span className="text-blue-600 mr-2">•</span>
                                    Claim your npm packages
                                </li>
                                <li className="flex items-start">
                                    <span className="text-blue-600 mr-2">•</span>
                                    Connect your payout method
                                </li>
                            </>
                        )}
                    </ul>
                </div>

                <button
                    onClick={handleContinue}
                    className="mt-8 w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
                >
                    Continue to Dashboard
                </button>
            </div>
        </div>
    );
};
