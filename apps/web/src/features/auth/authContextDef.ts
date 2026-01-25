import {createContext} from 'react';
import type {User} from './api';

export interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    setUser: (user: User | null) => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);
