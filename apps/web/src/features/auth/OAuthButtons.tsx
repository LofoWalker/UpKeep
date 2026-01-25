import React from 'react';
import {Button} from '@/components/ui/Button';
import {GitHubIcon} from '@/components/icons/GitHubIcon';
import {AccountType} from './api';
import {API_BASE_URL} from '@/lib/api';

interface OAuthButtonsProps {
    accountType?: AccountType;
    className?: string;
}

export const OAuthButtons: React.FC<OAuthButtonsProps> = ({
    accountType = AccountType.COMPANY,
    className = ''
}) => {
    const handleGitHubLogin = () => {
        const params = new URLSearchParams();
        params.append('accountType', accountType);
        window.location.href = `${API_BASE_URL}/api/auth/oauth/github?${params.toString()}`;
    };

    return (
        <div className={`space-y-3 ${className}`}>
            <Button
                type="button"
                variant="secondary"
                className="w-full flex items-center justify-center gap-2"
                onClick={handleGitHubLogin}
            >
                <GitHubIcon className="h-5 w-5"/>
                Continue with GitHub
            </Button>
        </div>
    );
};
