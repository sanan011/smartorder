import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Next.js Edge Middleware — runs before every request.
 *
 * Responsibilities:
 *  1. Redirect unauthenticated users away from protected routes.
 *  2. Redirect authenticated users away from auth pages.
 *  3. Enforce role-based access for dashboard routes.
 *
 * Token presence is checked via cookie (set by axios interceptor).
 * Full JWT validation happens at the API Gateway — middleware
 * only checks cookie existence for redirect decisions.
 */

// Routes that require authentication
const PROTECTED_ROUTES = [
    '/dashboard',
    '/orders',
    '/profile',
    '/checkout',
];

// Routes only accessible to sellers
const SELLER_ROUTES = ['/seller'];

// Routes only accessible to admins
const ADMIN_ROUTES = ['/admin'];

// Routes inaccessible once logged in
const AUTH_ROUTES = ['/auth/login', '/auth/register'];

export function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;

    const accessToken = request.cookies.get('so_access_token')?.value;
    const isAuthenticated = !!accessToken;

    // ── Redirect authenticated users away from auth pages ──────
    if (isAuthenticated && AUTH_ROUTES.some(r => pathname.startsWith(r))) {
        return NextResponse.redirect(new URL('/dashboard', request.url));
    }

    // ── Protect authenticated-only routes ──────────────────────
    const isProtected = PROTECTED_ROUTES.some(r => pathname.startsWith(r))
        || SELLER_ROUTES.some(r => pathname.startsWith(r))
        || ADMIN_ROUTES.some(r => pathname.startsWith(r));

    if (isProtected && !isAuthenticated) {
        const loginUrl = new URL('/auth/login', request.url);
        loginUrl.searchParams.set('redirect', pathname);
        return NextResponse.redirect(loginUrl);
    }

    // ── Role-based guards (basic — full check on server/API) ───
    // We read role from a non-HttpOnly cookie set at login time.
    // The API Gateway enforces the real authorization.
    if (isAuthenticated) {
        const userRole = request.cookies.get('so_user_role')?.value;

        if (ADMIN_ROUTES.some(r => pathname.startsWith(r)) && userRole !== 'ADMIN') {
            return NextResponse.redirect(new URL('/', request.url));
        }

        if (SELLER_ROUTES.some(r => pathname.startsWith(r))
            && userRole !== 'SELLER'
            && userRole !== 'ADMIN') {
            return NextResponse.redirect(new URL('/', request.url));
        }
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        /*
         * Match all paths except:
         * - _next/static (static files)
         * - _next/image  (image optimization)
         * - favicon.ico
         * - public assets
         * - API routes (handled by gateway rewrite)
         */
        '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
    ],
};