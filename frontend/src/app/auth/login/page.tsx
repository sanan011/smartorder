'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, ShoppingBag, Loader2 } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';
import Cookies from 'js-cookie';

// ── Validation schema ─────────────────────────────────────
const loginSchema = z.object({
    email:    z.string().email('Please enter a valid email address'),
    password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

// ── Component ─────────────────────────────────────────────
export default function LoginPage() {
    const router       = useRouter();
    const searchParams = useSearchParams();
    const redirect     = searchParams.get('redirect') ?? '/dashboard';

    const { login, isLoading, error, clearError, isAuthenticated, user } =
        useAuthStore();

    const [showPassword, setShowPassword] = useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
    });

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            router.replace(redirect);
        }
    }, [isAuthenticated, redirect, router]);

    // Clear store error when component unmounts
    useEffect(() => {
        return () => clearError();
    }, [clearError]);

    const onSubmit = async (data: LoginFormData) => {
        try {
            await login(data);

            // Store role in a readable cookie for middleware role checks
            if (user?.role) {
                Cookies.set('so_user_role', user.role, {
                    secure:   process.env.NODE_ENV === 'production',
                    sameSite: 'strict',
                    expires:  7,
                });
            }

            toast.success('Welcome back!');
            router.push(redirect);

        } catch {
            // Error already set in store — displayed below
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-primary-50 to-white
                    flex items-center justify-center p-4">
            <div className="w-full max-w-md">

                {/* Logo */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center gap-2 mb-2">
                        <ShoppingBag className="h-8 w-8 text-primary-600" />
                        <span className="text-2xl font-bold text-gray-900">SmartOrder</span>
                    </div>
                    <p className="text-gray-500 text-sm">Sign in to your account</p>
                </div>

                {/* Card */}
                <div className="card">
                    <h1 className="text-xl font-semibold text-gray-900 mb-6">Welcome back</h1>

                    {/* Server error */}
                    {error && (
                        <div className="mb-4 p-3 rounded-lg bg-danger-50 border border-danger-200
                            text-danger-600 text-sm animate-fade-in">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">

                        {/* Email */}
                        <div>
                            <label htmlFor="email"
                                   className="block text-sm font-medium text-gray-700 mb-1">
                                Email address
                            </label>
                            <input
                                id="email"
                                type="email"
                                autoComplete="email"
                                placeholder="you@example.com"
                                className={`input ${errors.email ? 'input-error' : ''}`}
                                {...register('email')}
                            />
                            {errors.email && (
                                <p className="mt-1 text-xs text-danger-500">{errors.email.message}</p>
                            )}
                        </div>

                        {/* Password */}
                        <div>
                            <div className="flex items-center justify-between mb-1">
                                <label htmlFor="password"
                                       className="block text-sm font-medium text-gray-700">
                                    Password
                                </label>
                                <Link href="/auth/forgot-password"
                                      className="text-xs text-primary-600 hover:underline">
                                    Forgot password?
                                </Link>
                            </div>
                            <div className="relative">
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    autoComplete="current-password"
                                    placeholder="••••••••"
                                    className={`input pr-10 ${errors.password ? 'input-error' : ''}`}
                                    {...register('password')}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(p => !p)}
                                    className="absolute inset-y-0 right-0 flex items-center pr-3
                             text-gray-400 hover:text-gray-600"
                                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                                >
                                    {showPassword
                                        ? <EyeOff className="h-4 w-4" />
                                        : <Eye    className="h-4 w-4" />}
                                </button>
                            </div>
                            {errors.password && (
                                <p className="mt-1 text-xs text-danger-500">{errors.password.message}</p>
                            )}
                        </div>

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="btn-primary w-full mt-2"
                        >
                            {isLoading
                                ? <><Loader2 className="h-4 w-4 animate-spin" /> Signing in...</>
                                : 'Sign in'}
                        </button>
                    </form>

                    {/* Register link */}
                    <p className="mt-6 text-center text-sm text-gray-500">
                        Don&apos;t have an account?{' '}
                        <Link href="/auth/register"
                              className="text-primary-600 font-medium hover:underline">
                            Create one
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}