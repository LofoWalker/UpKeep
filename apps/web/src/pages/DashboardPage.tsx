import React from 'react';
import {useAuth} from '../features/auth/useAuth';

export const DashboardPage: React.FC = () => {
    const {user, logout} = useAuth();

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
                    <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
                    <div className="flex items-center gap-4">
                        <span className="text-sm text-gray-600">{user?.email}</span>
                        <button
                            onClick={logout}
                            className="text-sm text-red-600 hover:text-red-500 font-medium"
                        >
                            Sign Out
                        </button>
                    </div>
                </div>
            </header>
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="bg-white rounded-lg shadow p-6">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">
                        Welcome, {user?.email}!
                    </h2>
                    <p className="text-gray-600">
                        Account type: <span className="font-medium">{user?.accountType}</span>
                    </p>
                </div>
            </main>
        </div>
    );
};
