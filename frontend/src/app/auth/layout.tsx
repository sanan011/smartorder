import type { Metadata } from 'next';

export const metadata: Metadata = {
    title: {
        template: '%s | SmartOrder',
        default:  'Auth | SmartOrder',
    },
};

/**
 * Shared layout for all auth pages (/auth/login, /auth/register, etc.)
 * Intentionally minimal — no navbar, no footer, just the page content.
 */
export default function AuthLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <main className="min-h-screen bg-gradient-to-br from-primary-50 to-white">
            {children}
        </main>
    );
}