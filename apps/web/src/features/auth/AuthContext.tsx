import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {getCurrentUser, loginCustomer, logoutCustomer, refreshToken, User} from './api';
import {AuthContext} from './authContextDef';

const USER_STORAGE_KEY = 'upkeep_user';

export function AuthProvider({children}: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const initAuth = async () => {
            try {
                // First try to get user from cookie-based session
                const currentUser = await getCurrentUser();
                setUser(currentUser);
                localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(currentUser));
            } catch {
                // If that fails, try to restore from localStorage and refresh token
                const storedUser = localStorage.getItem(USER_STORAGE_KEY);
                if (storedUser) {
                    try {
                        await refreshToken();
                        setUser(JSON.parse(storedUser));
                    } catch {
                        localStorage.removeItem(USER_STORAGE_KEY);
                    }
                }
            }
            setIsLoading(false);
        };
        initAuth();
    }, []);

    useEffect(() => {
        if (user) {
            localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
        } else {
            localStorage.removeItem(USER_STORAGE_KEY);
        }
    }, [user]);

    // Auto-refresh token before expiry
    useEffect(() => {
        if (!user) return;

        const REFRESH_INTERVAL = 13 * 60 * 1000; // 13 minutes (token expires in 15 minutes)
        const intervalId = setInterval(async () => {
            try {
                await refreshToken();
            } catch {
                setUser(null);
            }
        }, REFRESH_INTERVAL);

        return () => clearInterval(intervalId);
    }, [user]);

    const login = useCallback(async (email: string, password: string) => {
        const response = await loginCustomer({email, password});
        setUser({
            id: response.customerId,
            email: response.email,
            accountType: response.accountType,
        });
    }, []);

    const logout = useCallback(async () => {
        try {
            await logoutCustomer();
        } finally {
            setUser(null);
        }
    }, []);

    const value = useMemo(() => ({
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        setUser,
    }), [user, isLoading, login, logout]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

