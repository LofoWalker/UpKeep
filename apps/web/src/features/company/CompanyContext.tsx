/* eslint-disable react-refresh/only-export-components */
import {createContext, ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {
    CompanyDashboard,
    CompanyResponse,
    CompanyWithRole,
    createCompany as apiCreateCompany,
    CreateCompanyRequest,
    getCompanyDashboard,
    getUserCompanies
} from './api';
import {useAuth} from '@/features/auth';

interface CompanyContextType {
    companies: CompanyWithRole[];
    currentCompany: CompanyWithRole | null;
    dashboard: CompanyDashboard | null;
    isLoading: boolean;
    hasFetchedCompanies: boolean;
    error: string | null;
    setCurrentCompany: (company: CompanyWithRole) => void;
    refreshCompanies: () => Promise<void>;
    refreshDashboard: () => Promise<void>;
    createCompany: (data: CreateCompanyRequest) => Promise<CompanyResponse>;
}

const CompanyContext = createContext<CompanyContextType | undefined>(undefined);

interface CompanyProviderProps {
    children: ReactNode;
}

export function CompanyProvider({ children }: CompanyProviderProps) {
    const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
    const [companies, setCompanies] = useState<CompanyWithRole[]>([]);
    const [currentCompany, setCurrentCompanyState] = useState<CompanyWithRole | null>(null);
    const [dashboard, setDashboard] = useState<CompanyDashboard | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [hasFetchedCompanies, setHasFetchedCompanies] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const refreshCompanies = useCallback(async () => {
        if (!isAuthenticated) return;

        setIsLoading(true);
        setError(null);
        try {
            const data = await getUserCompanies();
            setCompanies(data);

            if (data.length > 0) {
                const storedCompanyId = localStorage.getItem('currentCompanyId');
                const stored = data.find(c => c.id === storedCompanyId);
                setCurrentCompanyState(prev => prev || stored || data[0]);
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load companies');
        } finally {
            setIsLoading(false);
            setHasFetchedCompanies(true);
        }
    }, [isAuthenticated]);

    const refreshDashboard = useCallback(async () => {
        if (!currentCompany) return;

        setIsLoading(true);
        setError(null);
        try {
            const data = await getCompanyDashboard(currentCompany.id);
            setDashboard(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load dashboard');
        } finally {
            setIsLoading(false);
        }
    }, [currentCompany]);

    const setCurrentCompany = useCallback((company: CompanyWithRole) => {
        setCurrentCompanyState(company);
        localStorage.setItem('currentCompanyId', company.id);
        setDashboard(null);
    }, []);

    const createCompany = useCallback(async (data: CreateCompanyRequest): Promise<CompanyResponse> => {
        const response = await apiCreateCompany(data);
        await refreshCompanies();
        return response;
    }, [refreshCompanies]);

    useEffect(() => {
        if (isAuthLoading) return;

        if (isAuthenticated && user) {
            refreshCompanies();
        } else {
            setCompanies([]);
            setCurrentCompanyState(null);
            setDashboard(null);
            setIsLoading(false);
            setHasFetchedCompanies(false);
        }
    }, [isAuthenticated, user, isAuthLoading, refreshCompanies]);

    useEffect(() => {
        if (currentCompany) {
            refreshDashboard();
        }
    }, [currentCompany, refreshDashboard]);

    return (
        <CompanyContext.Provider
            value={{
                companies,
                currentCompany,
                dashboard,
                isLoading,
                hasFetchedCompanies,
                error,
                setCurrentCompany,
                refreshCompanies,
                refreshDashboard,
                createCompany,
            }}
        >
            {children}
        </CompanyContext.Provider>
    );
}

export function useCompany() {
    const context = useContext(CompanyContext);
    if (context === undefined) {
        throw new Error('useCompany must be used within a CompanyProvider');
    }
    return context;
}
