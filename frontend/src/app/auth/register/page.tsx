'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
    Eye, EyeOff, ShoppingBag,
    Loader2, CheckCircle2,
} from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

// ── Validation schema ─────────────────────────────────────
const registerSchema = z.object({
    firstName: z
        .string()
        .min(1, 'First name is required')
        .max(100, 'First name is too long'),
    lastName: z
        .string()
        .min(1, 'Last name is required')
        .max(100, 'Last name is too long'),
    email: z
        .string()
        .email('Please enter a valid email address'),
    password: z
        .string()
        .min(8, 'Password must be at least 8 characters')
        .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
        .regex(/[0-9]/, 'Password must contain at least one number'),
    confirmPassword: z.string(),
    role: z.enum(['CUSTOMER', 'SELLER']),
    agreeToTerms: z
        .boolean()
        .refine(val => val === true, 'You must accept the terms and conditions'),
}).refine(data => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path:    ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

// ── Password strength indicator ───────────────────────────
function PasswordStrength({ password }: { password: string }) {
    const checks = [
        { label: 'At least 8 characters', pass: password.length >= 8 },
        { label: 'One uppercase letter',  pass: /[A-Z]/.test(password) },
        { label: 'One number',            pass: /[0-9]/.test(password) },
    ];

    if (!password) return null;

    return (
        <ul className="mt-2 space-y-1">
            {checks.map(({ label, pass }) => (
                <li key={label}
                    className={`flex items-center gap-1.5 text-xs
                        ${pass ? 'text-secondary-600' : 'text-gray-400'}`}>
                    <CheckCircle2 className={`h-3 w-3 ${pass ? 'text-secondary-500' : 'text-gray-300'}`} />
                    {label}
                </li>
            ))}
        </ul>
    );
}

// ── Component ─────────────────────────────────────────────
export default function RegisterPage() {
    const router = useRouter();

    const { register: registerUser, isLoading, error, clearError, isAuthenticated } =
        useAuthStore();

    const [showPassword,        setShowPassword]        = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [registered,          setRegistered]          = useState(false);
    const [registeredEmail,     setRegisteredEmail]     = useState('');

    const {
        register,
        handleSubmit,
        watch,
        formState: { errors },
    } = useForm<RegisterFormData>({
        resolver:     zodResolver(registerSchema),
        defaultValues: { role: 'CUSTOMER' },
    });

    const watchedPassword = watch('password', '');

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) router.replace('/dashboard');
    }, [isAuthenticated, router]);

    useEffect(() => {
        return () => clearError();
    }, [clearError]);

    const onSubmit = async (data: RegisterFormData) => {
        try {
            const response = await registerUser({
                email:     data.email,
                password:  data.password,
                firstName: data.firstName,
                lastName:  data.lastName,
                role:      data.role,
            });

            setRegisteredEmail(response.email);
            setRegistered(true);
            toast.success('Account created! Please check your email.');

        } catch {
            // Error displayed from store
        }
    };

    // ── Success state ─────────────────────────────────────────
    if (registered) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4">
                <div className="w-full max-w-md text-center card animate-fade-in">
                    <div className="flex justify-center mb-4">
                        <div className="h-16 w-16 rounded-full bg-secondary-50
                            flex items-center justify-center">
                            <CheckCircle2 className="h-8 w-8 text-secondary-500" />
                        </div>
                    </div>
                    <h2 className="text-xl font-semibold text-gray-900 mb-2">
                        Check your email
                    </h2>
                    <p className="text-gray-500 text-sm mb-6">
                        We sent a verification link to{' '}
                        <span className="font-medium text-gray-700">{registeredEmail}</span>.
                        Click the link to activate your account.
                    </p>
                    <Link href="/auth/login" className="btn-primary w-full justify-center">
                        Go to Login
                    </Link>
                </div>
            </div>
        );
    }

    // ── Registration form ─────────────────────────────────────
    return (
        <div className="min-h-screen flex items-center justify-center p-4 py-12">
            <div className="w-full max-w-md">

                {/* Logo */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center gap-2 mb-2">
                        <ShoppingBag className="h-8 w-8 text-primary-600" />
                        <span className="text-2xl font-bold text-gray-900">SmartOrder</span>
                    </div>
                    <p className="text-gray-500 text-sm">Create your account</p>
                </div>

                <div className="card">
                    <h1 className="text-xl font-semibold text-gray-900 mb-6">
                        Get started for free
                    </h1>

                    {/* Server error */}
                    {error && (
                        <div className="mb-4 p-3 rounded-lg bg-danger-50 border border-danger-200
                            text-danger-600 text-sm animate-fade-in">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">

                        {/* Role selector */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                I want to
                            </label>
                            <div className="grid grid-cols-2 gap-3">
                                {(['CUSTOMER', 'SELLER'] as const).map((r) => (
                                    <label key={r}
                                           className="relative flex cursor-pointer rounded-lg border p-3
                                    transition-colors has-[:checked]:border-primary-500
                                    has-[:checked]:bg-primary-50 border-gray-200">
                                        <input
                                            type="radio"
                                            value={r}
                                            className="sr-only"
                                            {...register('role')}
                                        />
                                        <div>
                                            <p className="text-sm font-medium text-gray-900">
                                                {r === 'CUSTOMER' ? 'Shop' : 'Sell'}
                                            </p>
                                            <p className="text-xs text-gray-500 mt-0.5">
                                                {r === 'CUSTOMER'
                                                    ? 'Buy from top sellers'
                                                    : 'List and sell products'}
                                            </p>
                                        </div>
                                    </label>
                                ))}
                            </div>
                        </div>

                        {/* Name row */}
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label htmlFor="firstName"
                                       className="block text-sm font-medium text-gray-700 mb-1">
                                    First name
                                </label>
                                <input
                                    id="firstName"
                                    type="text"
                                    autoComplete="given-name"
                                    className={`input ${errors.firstName ? 'input-error' : ''}`}
                                    {...register('firstName')}
                                />
                                {errors.firstName && (
                                    <p className="mt-1 text-xs text-danger-500">
                                        {errors.firstName.message}
                                    </p>
                                )}
                            </div>

                            <div>
                                <label htmlFor="lastName"
                                       className="block text-sm font-medium text-gray-700 mb-1">
                                    Last name
                                </label>
                                <input
                                    id="lastName"
                                    type="text"
                                    autoComplete="family-name"
                                    className={`input ${errors.lastName ? 'input-error' : ''}`}
                                    {...register('lastName')}
                                />
                                {errors.lastName && (
                                    <p className="mt-1 text-xs text-danger-500">
                                        {errors.lastName.message}
                                    </p>
                                )}
                            </div>
                        </div>

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
                            <label htmlFor="password"
                                   className="block text-sm font-medium text-gray-700 mb-1">
                                Password
                            </label>
                            <div className="relative">
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    autoComplete="new-password"
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
                            {errors.password
                                ? <p className="mt-1 text-xs text-danger-500">{errors.password.message}</p>
                                : <PasswordStrength password={watchedPassword} />
                            }
                        </div>

                        {/* Confirm password */}
                        <div>
                            <label htmlFor="confirmPassword"
                                   className="block text-sm font-medium text-gray-700 mb-1">
                                Confirm password
                            </label>
                            <div className="relative">
                                <input
                                    id="confirmPassword"
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    autoComplete="new-password"
                                    placeholder="••••••••"
                                    className={`input pr-10 ${errors.confirmPassword ? 'input-error' : ''}`}
                                    {...register('confirmPassword')}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(p => !p)}
                                    className="absolute inset-y-0 right-0 flex items-center pr-3
                             text-gray-400 hover:text-gray-600"
                                    aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                                >
                                    {showConfirmPassword
                                        ? <EyeOff className="h-4 w-4" />
                                        : <Eye    className="h-4 w-4" />}
                                </button>
                            </div>
                            {errors.confirmPassword && (
                                <p className="mt-1 text-xs text-danger-500">
                                    {errors.confirmPassword.message}
                                </p>
                            )}
                        </div>

                        {/* Terms */}
                        <div className="flex items-start gap-2">
                            <input
                                id="agreeToTerms"
                                type="checkbox"
                                className="mt-0.5 h-4 w-4 rounded border-gray-300
                           text-primary-600 focus:ring-primary-500"
                                {...register('agreeToTerms')}
                            />
                            <label htmlFor="agreeToTerms" className="text-sm text-gray-600">
                                I agree to the{' '}
                                <Link href="/terms"
                                      className="text-primary-600 hover:underline">
                                    Terms of Service
                                </Link>{' '}
                                and{' '}
                                <Link href="/privacy"
                                      className="text-primary-600 hover:underline">
                                    Privacy Policy
                                </Link>
                            </label>
                        </div>
                        {errors.agreeToTerms && (
                            <p className="text-xs text-danger-500 -mt-2">
                                {errors.agreeToTerms.message}
                            </p>
                        )}

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="btn-primary w-full mt-2"
                        >
                            {isLoading
                                ? <><Loader2 className="h-4 w-4 animate-spin" /> Creating account...</>
                                : 'Create account'}
                        </button>
                    </form>

                    {/* Login link */}
                    <p className="mt-6 text-center text-sm text-gray-500">
                        Already have an account?{' '}
                        <Link href="/auth/login"
                              className="text-primary-600 font-medium hover:underline">
                            Sign in
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}