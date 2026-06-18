'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
    LayoutDashboard, Package, ShoppingBag,
    BarChart2, Settings, ChevronRight,
} from 'lucide-react';
import { clsx } from 'clsx';

const NAV_ITEMS = [
    {
        href:  '/seller',
        label: 'Overview',
        icon:  LayoutDashboard,
        exact: true,
    },
    {
        href:  '/seller/products',
        label: 'Products',
        icon:  Package,
    },
    {
        href:  '/seller/orders',
        label: 'Orders',
        icon:  ShoppingBag,
    },
    {
        href:  '/seller/analytics',
        label: 'Analytics',
        icon:  BarChart2,
    },
    {
        href:  '/seller/settings',
        label: 'Settings',
        icon:  Settings,
    },
];

export default function SellerSidebar() {
    const pathname = usePathname();

    return (
        <aside className="w-56 flex-shrink-0">
            <nav className="card p-2 space-y-0.5">
                {NAV_ITEMS.map(({ href, label, icon: Icon, exact }) => {
                    const isActive = exact
                        ? pathname === href
                        : pathname.startsWith(href);

                    return (
                        <Link
                            key={href}
                            href={href}
                            className={clsx(
                                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm',
                                'transition-colors group',
                                isActive
                                    ? 'bg-primary-50 text-primary-700 font-medium'
                                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                            )}
                        >
                            <Icon className={clsx(
                                'h-4 w-4 flex-shrink-0',
                                isActive ? 'text-primary-600' : 'text-gray-400 group-hover:text-gray-600'
                            )} />
                            <span className="flex-1">{label}</span>
                            {isActive && (
                                <ChevronRight className="h-3 w-3 text-primary-400" />
                            )}
                        </Link>
                    );
                })}
            </nav>
        </aside>
    );
}