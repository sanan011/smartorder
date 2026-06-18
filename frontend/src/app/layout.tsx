import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Toaster } from 'react-hot-toast';

const inter = Inter({
    subsets: ['latin'],
    variable: '--font-inter',
    display: 'swap',
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
    openGraph: {
        type:      'website',
        siteName:  'SmartOrder',
        locale:    'en_US',
    },
};

export default function RootLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en" className={inter.variable}>
        <body className="min-h-screen bg-gray-50 font-sans antialiased">
        {children}
        <Toaster
            position="top-right"
            toastOptions={{
                duration: 4000,
                style: {
                    background: '#1e293b',
                    color:      '#f8fafc',
                    fontSize:   '14px',
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