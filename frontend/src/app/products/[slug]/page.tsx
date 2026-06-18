import type { Metadata } from 'next';
import Image from 'next/image';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import { Star, ShoppingCart, ArrowLeft, Package } from 'lucide-react';
import { productsApi } from '@/lib/api/products';
import AddToCartButton from '@/components/cart/AddToCartButton';

interface PageProps {
    params: { slug: string };
}

// ── SSR Metadata ──────────────────────────────────────────
export async function generateMetadata(
    { params }: PageProps
): Promise<Metadata> {
    try {
        const product = await productsApi.getBySlug(params.slug);
        const image   = product.images.find(i => i.primary)?.url;
        return {
            title:       product.name,
            description: product.description.slice(0, 160),
            openGraph: {
                title:       product.name,
                description: product.description.slice(0, 160),
                images:      image ? [{ url: image }] : [],
                type:        'website',
            },
        };
    } catch {
        return { title: 'Product Not Found' };
    }
}

// ── Server Component (SSR for SEO) ────────────────────────
export default async function ProductDetailPage({ params }: PageProps) {
    let product;
    try {
        product = await productsApi.getBySlug(params.slug);
    } catch {
        notFound();
    }

    const primaryImage = product.images.find(i => i.primary)
        ?? product.images[0];

    const discount = product.compareAtPrice
        ? Math.round((1 - product.price / product.compareAtPrice) * 100)
        : null;

    const formattedPrice = new Intl.NumberFormat('en-US', {
        style:    'currency',
        currency: product.currencyCode,
    }).format(product.price);

    const formattedCompare = product.compareAtPrice
        ? new Intl.NumberFormat('en-US', {
            style:    'currency',
            currency: product.currencyCode,
        }).format(product.compareAtPrice)
        : null;

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

                {/* Back link */}
                <Link href="/products"
                      className="inline-flex items-center gap-1.5 text-sm
                         text-gray-500 hover:text-gray-900 mb-6">
                    <ArrowLeft className="h-4 w-4" />
                    Back to products
                </Link>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">

                    {/* ── Images ─────────────────────────────────────── */}
                    <div className="space-y-3">
                        {/* Primary image */}
                        <div className="relative aspect-square rounded-2xl overflow-hidden
                            bg-gray-100">
                            {primaryImage ? (
                                <Image
                                    src={primaryImage.url}
                                    alt={primaryImage.altText || product.name}
                                    fill
                                    priority
                                    sizes="(max-width: 1024px) 100vw, 50vw"
                                    className="object-cover"
                                />
                            ) : (
                                <div className="h-full w-full flex items-center justify-center
                                text-gray-300">
                                    <Package className="h-20 w-20" />
                                </div>
                            )}

                            {discount && discount > 0 && (
                                <span className="absolute top-4 left-4 badge-red text-sm
                                 font-semibold px-3 py-1">
                  -{discount}% OFF
                </span>
                            )}
                        </div>

                        {/* Thumbnail strip */}
                        {product.images.length > 1 && (
                            <div className="grid grid-cols-5 gap-2">
                                {product.images.slice(0, 5).map(img => (
                                    <div key={img.id}
                                         className="relative aspect-square rounded-lg
                                  overflow-hidden bg-gray-100 cursor-pointer
                                  ring-2 ring-transparent hover:ring-primary-400
                                  transition-all">
                                        <Image
                                            src={img.url}
                                            alt={img.altText || product.name}
                                            fill
                                            sizes="80px"
                                            className="object-cover"
                                        />
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* ── Product info ────────────────────────────────── */}
                    <div className="flex flex-col gap-6">

                        {/* Brand + name */}
                        <div>
                            {product.brand && (
                                <p className="text-sm text-gray-400 uppercase tracking-widest mb-1">
                                    {product.brand}
                                </p>
                            )}
                            <h1 className="text-3xl font-bold text-gray-900 leading-tight">
                                {product.name}
                            </h1>
                        </div>

                        {/* Rating */}
                        {product.reviewCount > 0 && (
                            <div className="flex items-center gap-2">
                                <div className="flex">
                                    {Array.from({ length: 5 }).map((_, i) => (
                                        <Star
                                            key={i}
                                            className={`h-4 w-4 ${
                                                i < Math.round(product.averageRating)
                                                    ? 'fill-warning-500 text-warning-500'
                                                    : 'text-gray-200'
                                            }`}
                                        />
                                    ))}
                                </div>
                                <span className="text-sm font-medium text-gray-700">
                  {product.averageRating.toFixed(1)}
                </span>
                                <span className="text-sm text-gray-400">
                  ({product.reviewCount} reviews)
                </span>
                            </div>
                        )}

                        {/* Price */}
                        <div className="flex items-baseline gap-3">
              <span className="text-3xl font-bold text-gray-900">
                {formattedPrice}
              </span>
                            {formattedCompare && (
                                <span className="text-xl text-gray-400 line-through">
                  {formattedCompare}
                </span>
                            )}
                        </div>

                        {/* Tags */}
                        {product.tags.length > 0 && (
                            <div className="flex flex-wrap gap-2">
                                {product.tags.map(tag => (
                                    <Link
                                        key={tag}
                                        href={`/products?q=${encodeURIComponent(tag)}`}
                                        className="badge-blue hover:bg-primary-100 transition-colors"
                                    >
                                        {tag}
                                    </Link>
                                ))}
                            </div>
                        )}

                        {/* Add to cart */}
                        <div className="flex gap-3">
                            <AddToCartButton product={product} />
                        </div>

                        {/* SKU */}
                        {product.sku && (
                            <p className="text-xs text-gray-400">SKU: {product.sku}</p>
                        )}

                        {/* Description */}
                        <div className="border-t border-gray-100 pt-6">
                            <h2 className="text-base font-semibold text-gray-900 mb-3">
                                Description
                            </h2>
                            <div className="text-sm text-gray-600 leading-relaxed
                              whitespace-pre-line">
                                {product.description}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}