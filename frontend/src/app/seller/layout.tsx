import type { Metadata } from 'next';

export const metadata: Metadata = {
    title: {
        template: '%s | Seller Dashboard',
        default:  'Seller Dashboard',
    },
};

export default function SellerLayout({
                                         children,
                                     }: {
    children: React.ReactNode;
}) {
    return (
        <div className="min-h-screen bg-gray-50">
            {children}
        </div>
    );
}