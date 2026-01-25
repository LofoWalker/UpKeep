import './App.css'
import {BrowserRouter, Link, Route, Routes} from 'react-router-dom'
import {RegisterPage} from './pages/RegisterPage'
import {LoginPage} from './pages/LoginPage'
import {DashboardPage} from './pages/DashboardPage'
import {OnboardingPage} from './pages/OnboardingPage'
import {AuthProvider} from './features/auth/AuthContext'
import {ProtectedRoute} from './features/auth/ProtectedRoute'

function HomePage() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
            <div className="max-w-4xl mx-auto px-4 py-16">
                <h1 className="text-5xl font-bold text-gray-900 mb-4">
                    Upkeep
                </h1>
                <p className="text-xl text-gray-600 mb-8">
                    Open-source fund allocation platform for npm package maintainers
                </p>
                <div className="bg-white rounded-lg shadow-lg p-8">
                    <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                        Welcome to Upkeep
                    </h2>
                    <p className="text-gray-600 mb-4">
                        This monorepo is structured with a React + Vite frontend and a Quarkus backend,
                        following hexagonal architecture principles.
                    </p>
                    <div className="space-y-2 text-gray-600 mb-6">
                        <p>✓ Frontend: React 18 + Vite + TypeScript</p>
                        <p>✓ Styling: TailwindCSS + PostCSS</p>
                        <p>✓ Backend: Quarkus + Java 21</p>
                        <p>✓ Architecture: Hexagonal (Ports & Adapters)</p>
                    </div>
                    <div className="flex gap-4">
                        <Link
                            to="/login"
                            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition-colors"
                        >
                            Sign In
                        </Link>
                        <Link
                            to="/register"
                            className="bg-gray-200 text-gray-800 px-6 py-2 rounded-md hover:bg-gray-300 transition-colors"
                        >
                            Create Account
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    )
}

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route
                        path="/onboarding"
                        element={
                            <ProtectedRoute>
                                <OnboardingPage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <DashboardPage/>
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    )
}

export default App