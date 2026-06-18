'use client';

import { useEffect, useRef } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import {
    X, ShoppingCart, Minus, Plus,
    Trash2, Loader2, ArrowRight,
} from 'lucide-react';
import { useCartStore } from '@/store/cartStore';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

function formatPrice(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-US', {
        style: 'currency', currency,
    }).format(amount);
}

export default function CartDrawer() {
    const {
        cart, isOpen, isLoading,
        closeDrawer, updateItem, removeItem,
    } = useCartStore();

    const { isAuthenticated } = useAuthStore();
    const overlayRef = useRef<HTMLDivElement>(null);

    // Close on Escape
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.key === 'Escape') closeDrawer();
        };
        document.addEventListener('keydown', handler);
        return () => document.removeEventListener('keydown', handler);
    }, [closeDrawer]);

    // Prevent body scroll when open
    useEffect(() => {
        document.body.style.overflow = isOpen ? 'hidden' : '';
        return () => { document.body.style.overflow = ''; };
    }, [isOpen]);

    const handleQuantityChange = async (
        productId: string,
        newQty: number
    ) => {
        try {
            await updateItem(productId, newQty, !isAuthenticated);
        } catch {
            toast.error('Failed to update quantity.');
        }
    };

    const handleRemove = async (productId: string, name: string) => {
        try {
            await removeItem(productId, !isAuthenticated);
            toast.success(`${name} removed from cart.`);
        } catch {
            toast.error('Failed to remove item.');
        }
    };

    if (!isOpen) return null;

    return (
        <>
            {/* Backdrop */}
            <div
                ref={overlayRef}
                className="fixed inset-0 bg-black/40 z-50 animate-fade-in"
                onClick={closeDrawer}
                aria-hidden="true"
            />

            {/* Drawer panel */}
            <aside
                role="dialog"
                aria-modal="true"
                aria-label="Shopping cart"
                className="fixed right-0 top-0 h-full w-full max-w-md
                   bg-white shadow-2xl z-50 flex flex-col
                   animate-slide-up sm:animate-none"
            >
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4
                        border-b border-gray-100">
                    <div className="flex items-center gap-2">
                        <ShoppingCart className="h-5 w-5 text-gray-700" />
                        <h2 className="text-lg font-semibold text-gray-900">
                            Your Cart
                        </h2>
                        {cart && cart.totalItemCount > 0 && (
                            <span className="badge-blue">
                {cart.totalItemCount}
              </span>
                        )}
                    </div>
                    <button
                        onClick={closeDrawer}
                        className="p-2 rounded-lg text-gray-400
                       hover:text-gray-600 hover:bg-gray-100
                       transition-colors"
                        aria-label="Close cart"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* Body */}
                <div className="flex-1 overflow-y-auto px-6 py-4">
                    {isLoading && (
                        <div className="flex items-center justify-center py-12">
                            <Loader2 className="h-6 w-6 animate-spin text-primary-500" />
                        </div>
                    )}

                    {!isLoading && (!cart || cart.items.length === 0) && (
                        <div className="flex flex-col items-center justify-center
                            py-20 text-center">
                            <ShoppingCart className="h-12 w-12 text-gray-200 mb-4" />
                            <p className="text-gray-500 font-medium mb-1">
                                Your cart is empty
                            </p>
                            <p className="text-sm text-gray-400 mb-6">
                                Add some products to get started.
                            </p>
                            <button
                                onClick={closeDrawer}
                                className="btn-primary"
                            >
                                Continue shopping
                            </button>
                        </div>
                    )}

                    {!isLoading && cart && cart.items.length > 0 && (
                        <ul className="space-y-4">
                            {cart.items.map(item => (
                                <li
                                    key={item.productId}
                                    className="flex gap-4 py-4 border-b border-gray-50
                             last:border-0"
                                >
                                    {/* Image */}
                                    <Link
                                        href={`/products/${item.productSlug}`}
                                        onClick={closeDrawer}
                                        className="flex-shrink-0"
                                    >
                                        <div className="relative h-20 w-20 rounded-xl
                                    overflow-hidden bg-gray-100">
                                            {item.primaryImageUrl ? (
                                                <Image
                                                    src={item.primaryImageUrl}
                                                    alt={item.productName}
                                                    fill
                                                    sizes="80px"
                                                    className="object-cover"
                                                />
                                            ) : (
                                                <div className="h-full w-full flex items-center
                                        justify-center text-gray-300">
                                                    <ShoppingCart className="h-6 w-6" />
                                                </div>
                                            )}
                                        </div>
                                    </Link>

                                    {/* Details */}
                                    <div className="flex-1 min-w-0">
                                        <Link
                                            href={`/products/${item.productSlug}`}
                                            onClick={closeDrawer}
                                            className="text-sm font-medium text-gray-900
                                 hover:text-primary-600 line-clamp-2
                                 transition-colors"
                                        >
                                            {item.productName}
                                        </Link>

                                        <p className="text-sm font-semibold text-gray-900 mt-1">
                                            {formatPrice(item.unitPrice, item.currencyCode)}
                                        </p>

                                        {/* Quantity controls */}
                                        <div className="flex items-center justify-between mt-2">
                                            <div className="flex items-center gap-1">
                                                <button
                                                    onClick={() =>
                                                        handleQuantityChange(
                                                            item.productId,
                                                            item.quantity - 1
                                                        )
                                                    }
                                                    disabled={isLoading}
                                                    className="p-1 rounded-md border border-gray-200
                                     text-gray-500 hover:bg-gray-50
                                     disabled:opacity-50 transition-colors"
                                                    aria-label="Decrease quantity"
                                                >
                                                    <Minus className="h-3 w-3" />
                                                </button>
                                                <span className="w-8 text-center text-sm font-medium
                                         text-gray-900">
                          {item.quantity}
                        </span>
                                                <button
                                                    onClick={() =>
                                                        handleQuantityChange(
                                                            item.productId,
                                                            item.quantity + 1
                                                        )
                                                    }
                                                    disabled={isLoading || item.quantity >= 99}
                                                    className="p-1 rounded-md border border-gray-200
                                     text-gray-500 hover:bg-gray-50
                                     disabled:opacity-50 transition-colors"
                                                    aria-label="Increase quantity"
                                                >
                                                    <Plus className="h-3 w-3" />
                                                </button>
                                            </div>

                                            {/* Line total + remove */}
                                            <div className="flex items-center gap-3">
                        <span className="text-sm font-semibold text-gray-900">
                          {formatPrice(item.lineTotal, item.currencyCode)}
                        </span>
                                                <button
                                                    onClick={() =>
                                                        handleRemove(item.productId, item.productName)
                                                    }
                                                    disabled={isLoading}
                                                    className="p-1 text-gray-300 hover:text-danger-500
                                     disabled:opacity-50 transition-colors"
                                                    aria-label={`Remove ${item.productName}`}
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Footer — Order summary + CTA */}
                {cart && cart.items.length > 0 && (
                    <div className="border-t border-gray-100 px-6 py-4 space-y-4">

                        {/* Subtotals */}
                        <div className="space-y-1.5">
                            <div className="flex justify-between text-sm text-gray-600">
                                <span>Subtotal</span>
                                <span>
                  {formatPrice(cart.subtotal, cart.currencyCode)}
                </span>
                            </div>
                            {cart.discountAmount > 0 && (
                                <div className="flex justify-between text-sm text-secondary-600">
                                    <span>Discount</span>
                                    <span>
                    -{formatPrice(cart.discountAmount, cart.currencyCode)}
                  </span>
                                </div>
                            )}
                            <div className="flex justify-between text-base font-semibold
                              text-gray-900 pt-1 border-t border-gray-100">
                                <span>Total</span>
                                <span>
                  {formatPrice(cart.total, cart.currencyCode)}
                </span>
                            </div>
                        </div>

                        {/* Coupon code */}
                        {cart.couponCode && (
                            <div className="flex items-center gap-2 p-2 rounded-lg
                              bg-secondary-50 text-secondary-700 text-sm">
                                <span className="font-medium">Coupon:</span>
                                <span>{cart.couponCode}</span>
                            </div>
                        )}

                        {/* Checkout button */}
                        <Link
                            href="/checkout"
                            onClick={closeDrawer}
                            className="btn-primary w-full justify-center py-3 text-base"
                        >
                            Proceed to checkout
                            <ArrowRight className="h-4 w-4" />
                        </Link>

                        {/* Continue shopping */}
                        <button
                            onClick={closeDrawer}
                            className="w-full text-center text-sm text-gray-500
                         hover:text-gray-700 transition-colors"
                        >
                            Continue shopping
                        </button>
                    </div>
                )}
            </aside>
        </>
    );
}