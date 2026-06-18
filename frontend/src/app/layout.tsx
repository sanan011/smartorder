import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Toaster } from 'react-hot-toast';
import Navbar from '@/components/layout/Navbar';
import CartDrawer from '@/components/cart/CartDrawer';

const inter = Inter({
    subsets:  ['latin'],
    variable: '--font-inter',
    display:  'swap',
});

export const metadata: Metadata = {
    title: {
        default:  'SmartOrder — Multi-Vendor Marketplace',
        template: '%s | SmartOrder',
    },
    description: 'Discover thousands of products from verified sellers.',
    metadataBase: new URL(
        process.env.NEXT_PUBLIC_APP_URL ?? 'http://localhost:3000'
    ),
};

export default function RootLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en" className={inter.variable}>
        <body className="min-h-screen bg-gray-50 font-sans antialiased">
        <Navbar />
        <CartDrawer />
        {children}
        <Toaster
            position="top-right"
            toastOptions={{
                duration: 4000,
                style: {
                    background:   '#1e293b',
                    color:        '#f8fafc',
                    fontSize:     '14px',
                    borderRadius: '8px',
                },
                success: { iconTheme: { primary: '#22c55e', secondary: '#f8fafc' } },
                error:   { iconTheme: { primary: '#ef4444', secondary: '#f8fafc' } },
            }}
        />
        </body>
        </html>
    );
}