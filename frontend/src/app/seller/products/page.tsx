'use client';

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import {
    Plus, Pencil, Trash2, Send,
    Package, Loader2, AlertCircle,
} from 'lucide-react';
import SellerSidebar from '@/components/seller/SellerSidebar';
import { sellerApi } from '@/lib/api/seller';
import { useAuth } from '@/hooks/useAuth';
import type { Product } from '@/types/product';
import toast from 'react-hot-toast';
import { clsx } from 'clsx';

// ── Status badge ──────────────────────────────────────────
function StatusBadge({ status }: { status: string }) {
    const map: Record<string, string> = {
        DRAFT:    'badge-yellow',
        PENDING:  'badge-blue',
        ACTIVE:   'badge-green',
        INACTIVE: 'bg-gray-100 text-gray-600 badge',
        REJECTED: 'badge-red',
        DELETED:  'badge-red',
    };
    return (
        <span className={map[status] ?? 'badge'}>
      {status.toLowerCase()}
    </span>
    );
}

// ── Product row ───────────────────────────────────────────
function ProductRow({
                        product,
                        onDelete,
                        onSubmit,
                        isActing,
                    }: {
    product:  Product;
    onDelete: (id: string) => void;
    onSubmit: (id: string) => void;
    isActing: boolean;
}) {
    const primaryImage = product.images.find(i => i.primary)
        ?? product.images[0];

    const canSubmit = ['DRAFT', 'REJECTED'].includes(product.status);
    const canEdit   = product.status !== 'DELETED';

    return (
        <tr className="hover:bg-gray-50 transition-colors">
            {/* Image + name */}
            <td className="px-4 py-3">
                <div className="flex items-center gap-3">
                    <div className="relative h-10 w-10 rounded-lg overflow-hidden
                          bg-gray-100 flex-shrink-0">
                        {primaryImage ? (
                            <Image
                                src={primaryImage.url}
                                alt={product.name}
                                fill sizes="40px"
                                className="object-cover"
                            />
                        ) : (
                            <div className="h-full w-full flex items-center
                              justify-center">
                                <Package className="h-5 w-5 text-gray-300" />
                            </div>
                        )}
                    </div>
                    <div className="min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate
                          max-w-xs">
                            {product.name}
                        </p>
                        {product.sku && (
                            <p className="text-xs text-gray-400">SKU: {product.sku}</p>
                        )}
                    </div>
                </div>
            </td>

            {/* Status */}
            <td className="px-4 py-3">
                <StatusBadge status={product.status} />
            </td>

            {/* Price */}
            <td className="px-4 py-3 text-sm text-gray-700">
                {new Intl.NumberFormat('en-US', {
                    style:    'currency',
                    currency: product.currencyCode,
                }).format(product.price)}
            </td>

            {/* Rating */}
            <td className="px-4 py-3 text-sm text-gray-500">
                {product.reviewCount > 0
                    ? `${product.averageRating.toFixed(1)} (${product.reviewCount})`
                    : '—'}
            </td>

            {/* Actions */}
            <td className="px-4 py-3">
                <div className="flex items-center gap-1">
                    {canSubmit && (
                        <button
                            onClick={() => onSubmit(product.id)}
                            disabled={isActing}
                            title="Submit for review"
                            className="p-1.5 text-gray-400 hover:text-primary-600
                         disabled:opacity-50 transition-colors"
                        >
                            <Send className="h-4 w-4" />
                        </button>
                    )}
                    {canEdit && (
                        <Link
                            href={`/seller/products/${product.id}/edit`}
                            className="p-1.5 text-gray-400 hover:text-gray-700
                         transition-colors"
                            title="Edit product"
                        >
                            <Pencil className="h-4 w-4" />
                        </Link>
                    )}
                    <button
                        onClick={() => onDelete(product.id)}
                        disabled={isActing}
                        title="Delete product"
                        className="p-1.5 text-gray-400 hover:text-danger-500
                       disabled:opacity-50 transition-colors"
                    >
                        <Trash2 className="h-4 w-4" />
                    </button>
                </div>
            </td>
        </tr>
    );
}

// ── Page ──────────────────────────────────────────────────
const STATUS_TABS = [
    { label: 'All',      value: ''         },
    { label: 'Active',   value: 'ACTIVE'   },
    { label: 'Draft',    value: 'DRAFT'    },
    { label: 'Pending',  value: 'PENDING'  },
    { label: 'Rejected', value: 'REJECTED' },
];

export default function SellerProductsPage() {
    const { isAuthenticated, isSeller, isAdmin } = useAuth();

    const [products,    setProducts]    = useState<Product[]>([]);
    const [status,      setStatus]      = useState('');
    const [isLoading,   setIsLoading]   = useState(true);
    const [isActing,    setIsActing]    = useState(false);
    const [error,       setError]       = useState<string | null>(null);

    const loadProducts = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await sellerApi.getMyProducts({
                status: status || undefined,
                page: 0,
                size: 50,
            });
            setProducts(data);
        } catch {
            setError('Failed to load products. Please try again.');
        } finally {
            setIsLoading(false);
        }
    }, [status]);

    useEffect(() => { loadProducts(); }, [loadProducts]);

    const handleDelete = async (id: string) => {
        if (!confirm('Delete this product? This cannot be undone.')) return;
        setIsActing(true);
        try {
            await sellerApi.deleteProduct(id);
            toast.success('Product deleted');
            loadProducts();
        } catch {
            toast.error('Failed to delete product');
        } finally {
            setIsActing(false);
        }
    };

    const handleSubmit = async (id: string) => {
        setIsActing(true);
        try {
            await sellerApi.submitForReview(id);
            toast.success('Product submitted for review');
            loadProducts();
        } catch {
            toast.error('Failed to submit product');
        } finally {
            setIsActing(false);
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="flex gap-8">
                <SellerSidebar />

                <main className="flex-1 min-w-0">
                    {/* Header */}
                    <div className="flex items-center justify-between mb-6">
                        <h1 className="text-2xl font-bold text-gray-900">Products</h1>
                        <Link href="/seller/products/new" className="btn-primary">
                            <Plus className="h-4 w-4" /> Add product
                        </Link>
                    </div>

                    {/* Status tabs */}
                    <div className="flex gap-1 mb-4 bg-gray-100 rounded-lg p-1
                          w-fit">
                        {STATUS_TABS.map(tab => (
                            <button
                                key={tab.value}
                                onClick={() => setStatus(tab.value)}
                                className={clsx(
                                    'px-3 py-1.5 rounded-md text-sm font-medium transition-colors',
                                    status === tab.value
                                        ? 'bg-white text-gray-900 shadow-sm'
                                        : 'text-gray-500 hover:text-gray-700'
                                )}
                            >
                                {tab.label}
                            </button>
                        ))}
                    </div>

                    {/* Table */}
                    <div className="card p-0 overflow-hidden">
                        {isLoading ? (
                            <div className="flex items-center justify-center py-16">
                                <Loader2 className="h-6 w-6 animate-spin text-primary-600" />
                            </div>
                        ) : error ? (
                            <div className="flex items-center gap-2 p-6 text-danger-600">
                                <AlertCircle className="h-5 w-5" />
                                <span className="text-sm">{error}</span>
                            </div>
                        ) : products.length === 0 ? (
                            <div className="text-center py-16">
                                <Package className="h-10 w-10 text-gray-200 mx-auto mb-3" />
                                <p className="text-gray-500 text-sm mb-4">
                                    No products yet
                                </p>
                                <Link href="/seller/products/new"
                                      className="btn-primary text-sm">
                                    Add your first product
                                </Link>
                            </div>
                        ) : (
                            <table className="w-full">
                                <thead>
                                <tr className="border-b border-gray-100">
                                    {['Product', 'Status', 'Price',
                                        'Rating', 'Actions'].map(h => (
                                        <th key={h}
                                            className="px-4 py-3 text-left text-xs
                                     font-medium text-gray-500 uppercase
                                     tracking-wider">
                                            {h}
                                        </th>
                                    ))}
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-50">
                                {products.map(product => (
                                    <ProductRow
                                        key={product.id}
                                        product={product}
                                        onDelete={handleDelete}
                                        onSubmit={handleSubmit}
                                        isActing={isActing}
                                    />
                                ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </main>
            </div>
        </div>
    );
}