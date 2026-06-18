'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
    Package, ShoppingBag, DollarSign,
    TrendingUp, AlertCircle, Clock,
} from 'lucide-react';
import SellerSidebar from '@/components/seller/SellerSidebar';
import { useAuth } from '@/hooks/useAuth';

// ── Stat card ─────────────────────────────────────────────
function StatCard({
                      label,
                      value,
                      icon: Icon,
                      trend,
                      color = 'primary',
                  }: {
    label:  string;
    value:  string;
    icon:   React.ElementType;
    trend?: string;
    color?: 'primary' | 'secondary' | 'warning' | 'danger';
}) {
    const colorMap = {
        primary:   'bg-primary-50 text-primary-600',
        secondary: 'bg-secondary-50 text-secondary-600',
        warning:   'bg-warning-50 text-warning-600',
        danger:    'bg-danger-50 text-danger-600',
    };

    return (
        <div className="card">
            <div className="flex items-start justify-between">
                <div>
                    <p className="text-sm text-gray-500 mb-1">{label}</p>
                    <p className="text-2xl font-bold text-gray-900">{value}</p>
                    {trend && (
                        <p className="text-xs text-secondary-600 mt-1 flex items-center gap-1">
                            <TrendingUp className="h-3 w-3" />
                            {trend}
                        </p>
                    )}
                </div>
                <div className={`h-10 w-10 rounded-xl flex items-center
                         justify-center ${colorMap[color]}`}>
                    <Icon className="h-5 w-5" />
                </div>
            </div>
        </div>
    );
}

export default function SellerDashboardPage() {
    const { user, isAuthenticated, isLoading, isSeller, isAdmin } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading && !isAuthenticated) {
            router.replace('/auth/login?redirect=/seller');
        }
        if (!isLoading && isAuthenticated && !isSeller && !isAdmin) {
            router.replace('/');
        }
    }, [isLoading, isAuthenticated, isSeller, isAdmin, router]);

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-8 w-8
                        border-2 border-primary-600 border-t-transparent" />
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="flex gap-8">
                <SellerSidebar />

                <main className="flex-1 min-w-0">
                    {/* Header */}
                    <div className="mb-8">
                        <h1 className="text-2xl font-bold text-gray-900">
                            Welcome back, {user?.fullName?.split(' ')[0] ?? 'Seller'}
                        </h1>
                        <p className="text-gray-500 text-sm mt-1">
                            Here's what's happening with your store today.
                        </p>
                    </div>

                    {/* Stats grid */}
                    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                        <StatCard
                            label="Total Products"
                            value="—"
                            icon={Package}
                            color="primary"
                        />
                        <StatCard
                            label="Active Orders"
                            value="—"
                            icon={ShoppingBag}
                            color="secondary"
                        />
                        <StatCard
                            label="Revenue (30d)"
                            value="—"
                            icon={DollarSign}
                            color="warning"
                        />
                        <StatCard
                            label="Pending Review"
                            value="—"
                            icon={Clock}
                            color="danger"
                        />
                    </div>

                    {/* Quick actions */}
                    <div className="card">
                        <h2 className="text-base font-semibold text-gray-900 mb-4">
                            Quick Actions
                        </h2>
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                            {[
                                {
                                    href:  '/seller/products/new',
                                    label: 'Add new product',
                                    icon:  Package,
                                },
                                {
                                    href:  '/seller/orders',
                                    label: 'View orders',
                                    icon:  ShoppingBag,
                                },
                                {
                                    href:  '/seller/analytics',
                                    label: 'View analytics',
                                    icon:  BarChart2,
                                },
                            ].map(({ href, label, icon: Icon }) => (

                                key={href}
                                href={href}
                                className="flex items-center gap-3 p-3 rounded-lg
                                border border-gray-200 hover:border-primary-300
                                hover:bg-primary-50 transition-colors group"
                                >
                                <Icon className="h-4 w-4 text-gray-400
                                group-hover:text-primary-600" />
                                <span className="text-sm text-gray-700
                                group-hover:text-primary-700">
                            {label}
                                </span>
                                </a>
                                ))}
                        </div>
                    </div>

                    {/* Notice */}
                    <div className="mt-4 p-4 rounded-lg bg-warning-50 border
                          border-warning-200 flex items-start gap-3">
                        <AlertCircle className="h-4 w-4 text-warning-600 flex-shrink-0 mt-0.5" />
                        <p className="text-sm text-warning-700">
                            Analytics and order data will populate once you have
                            active products and orders.
                        </p>
                    </div>
                </main>
            </div>
        </div>
    );
}

// Fix missing import
function BarChart2(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg {...props} viewBox="0 0 24 24" fill="none"
             stroke="currentColor" strokeWidth={2}>
            <line x1="18" y1="20" x2="18" y2="10" />
            <line x1="12" y1="20" x2="12" y2="4"  />
            <line x1="6"  y1="20" x2="6"  y2="14" />
            <line x1="2"  y1="20" x2="22" y2="20" />
        </svg>
    );
}