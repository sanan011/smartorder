'use client';

import Link from 'next/link';
import { ShoppingBag, ShoppingCart, User, LogOut } from 'lucide-react';
import { useCartStore } from '@/store/cartStore';
import { useAuth } from '@/hooks/useAuth';
import { Suspense } from 'react';
import SearchBar from '@/components/product/SearchBar';

export default function Navbar() {
    const { toggleDrawer, itemCount } = useCartStore();
    const { user, isAuthenticated, logoutAndRedirect } = useAuth();

    const count = itemCount();

    return (
        <nav className="bg-white border-b border-gray-100 sticky top-0 z-40">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center gap-4 h-16">

                    {/* Logo */}
                    <Link href="/"
                          className="flex items-center gap-2 flex-shrink-0">
                        <ShoppingBag className="h-6 w-6 text-primary-600" />
                        <span className="font-bold text-gray-900 hidden sm:block">
              SmartOrder
            </span>
                    </Link>

                    {/* Search */}
                    <div className="flex-1 max-w-xl">
                        <Suspense>
                            <SearchBar />
                        </Suspense>
                    </div>

                    {/* Right actions */}
                    <div className="flex items-center gap-2 flex-shrink-0">

                        {/* Cart button */}
                        <button
                            onClick={toggleDrawer}
                            className="relative p-2 rounded-lg text-gray-600
                         hover:bg-gray-100 transition-colors"
                            aria-label={`Cart (${count} items)`}
                        >
                            <ShoppingCart className="h-5 w-5" />
                            {count > 0 && (
                                <span className="absolute -top-1 -right-1 h-4 w-4
                                 rounded-full bg-primary-600 text-white
                                 text-xs flex items-center justify-center
                                 font-medium">
                  {count > 99 ? '99+' : count}
                </span>
                            )}
                        </button>

                        {/* Auth */}
                        {isAuthenticated ? (
                            <div className="flex items-center gap-1">
                                <Link
                                    href="/dashboard"
                                    className="flex items-center gap-1.5 px-3 py-2
                             rounded-lg text-sm font-medium text-gray-700
                             hover:bg-gray-100 transition-colors"
                                >
                                    <User className="h-4 w-4" />
                                    <span className="hidden md:block">
                    {user?.fullName?.split(' ')[0] ?? 'Account'}
                  </span>
                                </Link>
                                <button
                                    onClick={() => logoutAndRedirect()}
                                    className="p-2 rounded-lg text-gray-400
                             hover:text-gray-600 hover:bg-gray-100
                             transition-colors"
                                    aria-label="Sign out"
                                >
                                    <LogOut className="h-4 w-4" />
                                </button>
                            </div>
                        ) : (
                            <div className="flex items-center gap-2">
                                <Link href="/auth/login"
                                      className="btn-secondary py-1.5 px-3 text-sm">
                                    Sign in
                                </Link>
                                <Link href="/auth/register"
                                      className="btn-primary py-1.5 px-3 text-sm
                                 hidden sm:flex">
                                    Register
                                </Link>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
}