'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback } from 'react';
import { X } from 'lucide-react';

interface FilterOption {
    label: string;
    value: string;
}

const SORT_OPTIONS: FilterOption[] = [
    { label: 'Newest',       value: 'newest'     },
    { label: 'Price: Low',   value: 'price_asc'  },
    { label: 'Price: High',  value: 'price_desc' },
    { label: 'Top Rated',    value: 'rating'     },
];

interface Props {
    totalHits: number;
}

export default function ProductFilters({ totalHits }: Props) {
    const router       = useRouter();
    const searchParams = useSearchParams();

    const currentSort     = searchParams.get('sortBy')   ?? 'newest';
    const currentMinPrice = searchParams.get('minPrice') ?? '';
    const currentMaxPrice = searchParams.get('maxPrice') ?? '';

    const updateParams = useCallback((updates: Record<string, string | null>) => {
        const params = new URLSearchParams(searchParams.toString());
        Object.entries(updates).forEach(([key, value]) => {
            if (value === null || value === '') {
                params.delete(key);
            } else {
                params.set(key, value);
            }
        });
        params.delete('page'); // reset to page 0 on filter change
        router.push(`?${params.toString()}`);
    }, [searchParams, router]);

    const hasActiveFilters = currentMinPrice || currentMaxPrice;

    return (
        <div className="flex flex-wrap items-center gap-3">

            {/* Results count */}
            <span className="text-sm text-gray-500">
        {totalHits.toLocaleString()} result{totalHits !== 1 ? 's' : ''}
      </span>

            <div className="flex-1" />

            {/* Price range */}
            <div className="flex items-center gap-2">
                <input
                    type="number"
                    placeholder="Min $"
                    value={currentMinPrice}
                    onChange={e => updateParams({ minPrice: e.target.value })}
                    className="input w-24 text-sm py-1.5"
                    min={0}
                />
                <span className="text-gray-400 text-sm">–</span>
                <input
                    type="number"
                    placeholder="Max $"
                    value={currentMaxPrice}
                    onChange={e => updateParams({ maxPrice: e.target.value })}
                    className="input w-24 text-sm py-1.5"
                    min={0}
                />
            </div>

            {/* Clear filters */}
            {hasActiveFilters && (
                <button
                    onClick={() => updateParams({ minPrice: null, maxPrice: null })}
                    className="flex items-center gap-1 text-xs text-danger-500
                     hover:text-danger-600"
                >
                    <X className="h-3 w-3" /> Clear
                </button>
            )}

            {/* Sort */}
            <select
                value={currentSort}
                onChange={e => updateParams({ sortBy: e.target.value })}
                className="input w-auto text-sm py-1.5 pr-8"
            >
                {SORT_OPTIONS.map(opt => (
                    <option key={opt.value} value={opt.value}>
                        {opt.label}
                    </option>
                ))}
            </select>
        </div>
    );
}