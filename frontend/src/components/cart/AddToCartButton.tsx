'use client';

import { useState } from 'react';
import { ShoppingCart, Check, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import type { Product } from '@/types/product';

interface Props {
    product: Product;
}

/**
 * Placeholder AddToCartButton — wired to the cart store in Phase 4.
 * Shows success feedback immediately for UX purposes.
 */
export default function AddToCartButton({ product }: Props) {
    const [state, setState] = useState<'idle' | 'loading' | 'added'>('idle');

    const handleClick = async () => {
        setState('loading');
        // Cart store integration added in Phase 4
        await new Promise(r => setTimeout(r, 400));
        setState('added');
        toast.success(`${product.name} added to cart!`);
        setTimeout(() => setState('idle'), 2000);
    };

    return (
        <button
            onClick={handleClick}
            disabled={state === 'loading' || state === 'added'}
            className="btn-primary flex-1 py-3 text-base"
        >
            {state === 'loading' && (
                <><Loader2 className="h-5 w-5 animate-spin" /> Adding...</>
            )}
            {state === 'added' && (
                <><Check className="h-5 w-5" /> Added to cart</>
            )}
            {state === 'idle' && (
                <><ShoppingCart className="h-5 w-5" /> Add to cart</>
            )}
        </button>
    );
}