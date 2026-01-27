import {OnboardingLayout} from '@/components/layout';
import {CreateCompanyForm} from '@/features/company/CreateCompanyForm';

const steps = [
    { id: 'company', label: 'Create Company' },
    { id: 'budget', label: 'Set Budget' },
    { id: 'packages', label: 'Import Packages' },
];

export function CreateCompanyPage() {
    return (
        <OnboardingLayout steps={steps} currentStep={0}>
            <CreateCompanyForm />
        </OnboardingLayout>
    );
}
