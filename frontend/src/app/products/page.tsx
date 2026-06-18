import { Suspense } from 'react';
import type { Metadata } from 'next';
import ProductCard from '@/components/product/ProductCard';
import ProductFilters from '@/components/product/ProductFilters';
import SearchBar from '@/components/product/SearchBar';
import { productsApi } from '@/lib/api/products';
import { ShoppingBag } from 'lucide-react';
import Link from 'next/link';

interface PageProps {
    searchParams: {
        q?:        string;
        category?: string;
        minPrice?: string;
        maxPrice?: string;
        sortBy?:   string;
        page?:     string;
    };
}

export async function generateMetadata(
    { searchParams }: PageProps
): Promise<Metadata> {
    const q = searchParams.q;
    return {
        title: q ? `"${q}" — Search Results` : 'Browse Products',
        description: 'Discover thousands of products from verified sellers.',
    };
}

// ── Server Component — SSR for SEO ────────────────────────
export default async function ProductsPage({ searchParams }: PageProps) {
    const page = parseInt(searchParams.page ?? '0', 10);
    const size = 20;

    let result;
    try {
        result = await productsApi.search({
            q:        searchParams.q,
            category: searchParams.category,
            minPrice: searchParams.minPrice
                ? parseFloat(searchParams.minPrice) : undefined,
            maxPrice: searchParams.maxPrice
                ? parseFloat(searchParams.maxPrice) : undefined,
            sortBy:   searchParams.sortBy ?? 'newest',
            page,
            size,
        });
    } catch {
        result = { products: [], totalHits: 0, page: 0, size, totalPages: 0 };
    }

    return (
        <div className="min-h-screen bg-gray-50">

            {/* Header */}
            <header className="bg-white border-b border-gray-100 sticky top-0 z-40">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center gap-4">
                        <Link href="/" className="flex items-center gap-2 flex-shrink-0">
                            <ShoppingBag className="h-6 w-6 text-primary-600" />
                            <span className="font-bold text-gray-900 hidden sm:block">
                SmartOrder
              </span>
                        </Link>
                        <div className="flex-1">
                            <Suspense>
                                <SearchBar />
                            </Suspense>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

                {/* Page title */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-900">
                        {searchParams.q
                            ? `Results for "${searchParams.q}"`
                            : 'All Products'}
                    </h1>
                </div>

                {/* Filters bar */}
                <div className="mb-6">
                    <Suspense>
                        <ProductFilters totalHits={result.totalHits} />
                    </Suspense>
                </div>

                {/* Product grid */}
                {result.products.length === 0 ? (
                    <EmptyState query={searchParams.q} />
                ) : (
                    <>
                        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4
                            xl:grid-cols-5 gap-4">
                            {result.products.map(product => (
                                <ProductCard key={product.id} product={product} />
                            ))}
                        </div>

                        {/* Pagination */}
                        {result.totalPages > 1 && (
                            <Pagination
                                currentPage={page}
                                totalPages={result.totalPages}
                                searchParams={searchParams}
                            />
                        )}
                    </>
                )}
            </main>
        </div>
    );
}

// ── Empty state ───────────────────────────────────────────
function EmptyState({ query }: { query?: string }) {
    return (
        <div className="text-center py-20">
            <ShoppingBag className="h-12 w-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
                {query ? `No results for "${query}"` : 'No products found'}
            </h3>
            <p className="text-gray-500 text-sm mb-6">
                Try adjusting your search or filters.
            </p>
            <Link href="/products" className="btn-primary">
                Browse all products
            </Link>
        </div>
    );
}

// ── Pagination ────────────────────────────────────────────
function Pagination({
                        currentPage,
                        totalPages,
                        searchParams,
                    }: {
    currentPage:  number;
    totalPages:   number;
    searchParams: Record<string, string | undefined>;
}) {
    const buildPageUrl = (p: number) => {
        const params = new URLSearchParams();
        Object.entries(searchParams).forEach(([k, v]) => {
            if (v) params.set(k, v);
        });
        params.set('page', p.toString());
        return `/products?${params.toString()}`;
    };

    const pages = Array.from({ length: Math.min(totalPages, 7) }, (_, i) => i);

    return (
        <nav className="mt-10 flex justify-center gap-2" aria-label="Pagination">
            {currentPage > 0 && (
                <Link href={buildPageUrl(currentPage - 1)}
                      className="btn-secondary px-3 py-2 text-sm">
                    Previous
                </Link>
            )}
            {pages.map(p => (
                <Link
                    key={p}
                    href={buildPageUrl(p)}
                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors
            ${p === currentPage
                        ? 'bg-primary-600 text-white'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'}`}
                >
                    {p + 1}
                </Link>
            ))}
            {currentPage < totalPages - 1 && (
                <Link href={buildPageUrl(currentPage + 1)}
                      className="btn-secondary px-3 py-2 text-sm">
                    Next
                </Link>
            )}
        </nav>
    );
}