import Link from 'next/link';
import { ShoppingBag, ArrowRight, Star, Shield, Truck } from 'lucide-react';

/**
 * Public homepage — server component, fully SSR.
 * No auth required.
 */
export default function HomePage() {
    return (
        <div className="min-h-screen bg-white">

            {/* Nav */}
            <nav className="border-b border-gray-100 px-6 py-4">
                <div className="max-w-6xl mx-auto flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <ShoppingBag className="h-6 w-6 text-primary-600" />
                        <span className="text-lg font-bold text-gray-900">SmartOrder</span>
                    </div>
                    <div className="flex items-center gap-4">
                        <Link href="/products"
                              className="text-sm text-gray-600 hover:text-gray-900">
                            Browse
                        </Link>
                        <Link href="/auth/login"
                              className="btn-secondary text-sm py-1.5 px-3">
                            Sign in
                        </Link>
                        <Link href="/auth/register"
                              className="btn-primary text-sm py-1.5 px-3">
                            Get started
                        </Link>
                    </div>
                </div>
            </nav>

            {/* Hero */}
            <section className="max-w-6xl mx-auto px-6 py-24 text-center">
                <h1 className="text-5xl font-bold text-gray-900 mb-6 leading-tight">
                    The smarter way to{' '}
                    <span className="text-primary-600">shop & sell</span>
                </h1>
                <p className="text-xl text-gray-500 mb-10 max-w-2xl mx-auto">
                    Discover thousands of products from verified sellers.
                    Fast delivery, secure payments, and world-class support.
                </p>
                <div className="flex items-center justify-center gap-4 flex-wrap">
                    <Link href="/products" className="btn-primary px-8 py-3 text-base">
                        Start shopping <ArrowRight className="h-4 w-4" />
                    </Link>
                    <Link href="/auth/register?role=SELLER"
                          className="btn-secondary px-8 py-3 text-base">
                        Become a seller
                    </Link>
                </div>
            </section>

            {/* Features */}
            <section className="bg-gray-50 py-20">
                <div className="max-w-6xl mx-auto px-6">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                        {[
                            {
                                icon:  <Star  className="h-6 w-6 text-primary-600" />,
                                title: 'Verified Sellers',
                                desc:  'Every seller is vetted before listing products on our platform.',
                            },
                            {
                                icon:  <Shield className="h-6 w-6 text-primary-600" />,
                                title: 'Secure Payments',
                                desc:  'Your payment data is encrypted and never stored on our servers.',
                            },
                            {
                                icon:  <Truck className="h-6 w-6 text-primary-600" />,
                                title: 'Fast Delivery',
                                desc:  'Real-time tracking from warehouse to your door.',
                            },
                        ].map(({ icon, title, desc }) => (
                            <div key={title} className="card text-center">
                                <div className="inline-flex items-center justify-center
                                h-12 w-12 rounded-xl bg-primary-50 mb-4">
                                    {icon}
                                </div>
                                <h3 className="font-semibold text-gray-900 mb-2">{title}</h3>
                                <p className="text-sm text-gray-500">{desc}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

        </div>
    );
}