import React, {createContext, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {loginCustomer, logoutCustomer, refreshToken, User} from './api';

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const USER_STORAGE_KEY = 'upkeep_user';

export function AuthProvider({children}: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(() => {
        const stored = localStorage.getItem(USER_STORAGE_KEY);
        return stored ? JSON.parse(stored) : null;
    });
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const initAuth = async () => {
            const storedUser = localStorage.getItem(USER_STORAGE_KEY);
            if (storedUser) {
                try {
                    await refreshToken();
                    setUser(JSON.parse(storedUser));
                } catch {
                    localStorage.removeItem(USER_STORAGE_KEY);
                    setUser(null);
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

        const REFRESH_INTERVAL = 13 * 60 * 1000; // 13 minutes (token expires at 15)
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

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
}
