'use client';

import { useState } from 'react';
import { ShoppingCart, Check, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { useCartStore } from '@/store/cartStore';
import { useAuthStore } from '@/store/authStore';
import type { Product } from '@/types/product';

interface Props {
    product: Product;
}

export default function AddToCartButton({ product }: Props) {
    const [state, setState] = useState<'idle' | 'loading' | 'added'>('idle');

    const { addItem }        = useCartStore();
    const { isAuthenticated } = useAuthStore();

    const primaryImage = product.images.find(i => i.primary)?.url
        ?? product.images[0]?.url
        ?? null;

    const handleClick = async () => {
        setState('loading');
        try {
            await addItem(
                {
                    productId:       product.id,
                    productName:     product.name,
                    productSlug:     product.slug,
                    primaryImageUrl: primaryImage,
                    unitPrice:       product.price,
                    currencyCode:    product.currencyCode,
                    quantity:        1,
                    sellerId:        product.sellerId,
                },
                !isAuthenticated
            );
            setState('added');
            toast.success(`${product.name} added to cart!`);
            setTimeout(() => setState('idle'), 2000);
        } catch {
            setState('idle');
            toast.error('Failed to add item to cart.');
        }
    };

    return (
        <button
            onClick={handleClick}
            disabled={state !== 'idle'}
            className="btn-primary flex-1 py-3 text-base"
        >
            {state === 'loading' && (
                <><Loader2 className="h-5 w-5 animate-spin" /> Adding...</>
            )}
            {state === 'added' && (
                <><Check className="h-5 w-5" /> Added!</>
            )}
            {state === 'idle' && (
                <><ShoppingCart className="h-5 w-5" /> Add to cart</>
            )}
        </button>
    );
}