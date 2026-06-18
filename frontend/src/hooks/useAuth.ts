import { useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import type { User } from '@/types/auth';

/**
 * Convenience hook that wraps the auth store and exposes
 * role-based helper methods used throughout the app.
 *
 * Usage:
 *   const { user, isAdmin, requireAuth } = useAuth();
 */
export function useAuth() {
    const router = useRouter();
    const {
        user,
        isAuthenticated,
        isLoading,
        error,
        login,
        logout,
        clearError,
    } = useAuthStore();

    // ── Role helpers ─────────────────────────────────────────
    const isCustomer = user?.role === 'CUSTOMER';
    const isSeller   = user?.role === 'SELLER';
    const isAdmin    = user?.role === 'ADMIN';
    const isSupport  = user?.role === 'SUPPORT';

    const hasRole = useCallback(
        (...roles: User['role'][]) => !!user && roles.includes(user.role),
        [user]
    );

    const canManageProducts = isAdmin || isSeller;
    const canViewAdminPanel = isAdmin || isSupport;

    // ── Guards ───────────────────────────────────────────────

    /**
     * Redirects to login if not authenticated.
     * Call this at the top of any protected client component.
     */
    const requireAuth = useCallback(
        (redirectTo = '/dashboard') => {
            if (!isLoading && !isAuthenticated) {
                router.push(
                    `/auth/login?redirect=${encodeURIComponent(redirectTo)}`
                );
                return false;
            }
            return true;
        },
        [isAuthenticated, isLoading, router]
    );

    /**
     * Redirects to home if user does not have one of the required roles.
     */
    const requireRole = useCallback(
        (...roles: User['role'][]) => {
            if (!isLoading && (!isAuthenticated || !hasRole(...roles))) {
                router.push('/');
                return false;
            }
            return true;
        },
        [isAuthenticated, isLoading, hasRole, router]
    );

    // ── Logout helper ─────────────────────────────────────────
    const logoutAndRedirect = useCallback(
        async (redirectTo = '/auth/login') => {
            await logout();
            router.push(redirectTo);
        },
        [logout, router]
    );

    return {
        // State
        user,
        isAuthenticated,
        isLoading,
        error,

        // Role flags
        isCustomer,
        isSeller,
        isAdmin,
        isSupport,
        hasRole,

        // Permission shortcuts
        canManageProducts,
        canViewAdminPanel,

        // Actions
        login,
        logout,
        logoutAndRedirect,
        clearError,

        // Guards
        requireAuth,
        requireRole,
    };
}