import Link from 'next/link';
import Image from 'next/image';
import { Star, ShoppingCart } from 'lucide-react';
import type { Product } from '@/types/product';

interface Props {
    product: Product;
}

function formatPrice(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-US', {
        style:    'currency',
        currency: currency,
    }).format(amount);
}

export default function ProductCard({ product }: Props) {
    const primaryImage = product.images.find(img => img.primary)
        ?? product.images[0];

    const discount = product.compareAtPrice
        ? Math.round((1 - product.price / product.compareAtPrice) * 100)
        : null;

    return (
        <Link
            href={`/products/${product.slug}`}
            className="card-hover group flex flex-col overflow-hidden p-0"
        >
            {/* Image */}
            <div className="relative aspect-square bg-gray-100 overflow-hidden">
                {primaryImage ? (
                    <Image
                        src={primaryImage.url}
                        alt={primaryImage.altText || product.name}
                        fill
                        sizes="(max-width: 640px) 100vw,
                   (max-width: 1024px) 50vw,
                   25vw"
                        className="object-cover transition-transform duration-300
                       group-hover:scale-105"
                    />
                ) : (
                    <div className="h-full w-full flex items-center justify-center
                          bg-gray-100 text-gray-300">
                        <ShoppingCart className="h-12 w-12" />
                    </div>
                )}

                {/* Discount badge */}
                {discount && discount > 0 && (
                    <span className="absolute top-2 left-2 badge-red text-xs font-semibold">
            -{discount}%
          </span>
                )}
            </div>

            {/* Content */}
            <div className="p-4 flex flex-col gap-2 flex-1">
                {/* Brand */}
                {product.brand && (
                    <p className="text-xs text-gray-400 uppercase tracking-wide">
                        {product.brand}
                    </p>
                )}

                {/* Name */}
                <h3 className="text-sm font-medium text-gray-900 line-clamp-2
                       group-hover:text-primary-600 transition-colors">
                    {product.name}
                </h3>

                {/* Rating */}
                {product.reviewCount > 0 && (
                    <div className="flex items-center gap-1">
                        <Star className="h-3.5 w-3.5 fill-warning-500 text-warning-500" />
                        <span className="text-xs text-gray-600">
              {product.averageRating.toFixed(1)}
            </span>
                        <span className="text-xs text-gray-400">
              ({product.reviewCount})
            </span>
                    </div>
                )}

                {/* Price */}
                <div className="mt-auto flex items-baseline gap-2">
          <span className="text-base font-semibold text-gray-900">
            {formatPrice(product.price, product.currencyCode)}
          </span>
                    {product.compareAtPrice && (
                        <span className="text-sm text-gray-400 line-through">
              {formatPrice(product.compareAtPrice, product.currencyCode)}
            </span>
                    )}
                </div>
            </div>
        </Link>
    );
}