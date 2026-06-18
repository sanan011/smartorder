'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Search, X, Loader2 } from 'lucide-react';
import { productsApi } from '@/lib/api/products';

function useDebounce<T>(value: T, delay: number): T {
    const [debounced, setDebounced] = useState(value);
    useEffect(() => {
        const timer = setTimeout(() => setDebounced(value), delay);
        return () => clearTimeout(timer);
    }, [value, delay]);
    return debounced;
}

export default function SearchBar() {
    const router       = useRouter();
    const searchParams = useSearchParams();

    const [query,       setQuery]       = useState(searchParams.get('q') ?? '');
    const [suggestions, setSuggestions] = useState<string[]>([]);
    const [isLoading,   setIsLoading]   = useState(false);
    const [isOpen,      setIsOpen]      = useState(false);
    const [activeIndex, setActiveIndex] = useState(-1);

    const inputRef     = useRef<HTMLInputElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);

    const debouncedQuery = useDebounce(query, 300);

    // ── Fetch autocomplete suggestions ───────────────────────
    useEffect(() => {
        if (debouncedQuery.trim().length < 2) {
            setSuggestions([]);
            setIsOpen(false);
            return;
        }

        setIsLoading(true);
        productsApi.autocomplete(debouncedQuery)
            .then(results => {
                setSuggestions(results);
                setIsOpen(results.length > 0);
                setActiveIndex(-1);
            })
            .catch(() => setSuggestions([]))
            .finally(() => setIsLoading(false));
    }, [debouncedQuery]);

    // ── Close dropdown on outside click ──────────────────────
    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (containerRef.current &&
                !containerRef.current.contains(e.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSearch = useCallback((term: string) => {
        if (!term.trim()) return;
        setIsOpen(false);
        router.push(`/products?q=${encodeURIComponent(term.trim())}`);
    }, [router]);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (!isOpen) return;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setActiveIndex(i => Math.min(i + 1, suggestions.length - 1));
                break;
            case 'ArrowUp':
                e.preventDefault();
                setActiveIndex(i => Math.max(i - 1, -1));
                break;
            case 'Enter':
                e.preventDefault();
                if (activeIndex >= 0) {
                    handleSearch(suggestions[activeIndex]);
                    setQuery(suggestions[activeIndex]);
                } else {
                    handleSearch(query);
                }
                break;
            case 'Escape':
                setIsOpen(false);
                setActiveIndex(-1);
                break;
        }
    };

    const handleClear = () => {
        setQuery('');
        setSuggestions([]);
        setIsOpen(false);
        inputRef.current?.focus();
    };

    return (
        <div ref={containerRef} className="relative w-full max-w-2xl">
            {/* Input */}
            <div className="relative flex items-center">
                <Search className="absolute left-3 h-4 w-4 text-gray-400 pointer-events-none" />
                <input
                    ref={inputRef}
                    type="search"
                    value={query}
                    onChange={e => setQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    onFocus={() => suggestions.length > 0 && setIsOpen(true)}
                    placeholder="Search products..."
                    className="input pl-9 pr-10"
                    aria-label="Search products"
                    aria-autocomplete="list"
                    aria-expanded={isOpen}
                    aria-controls="search-suggestions"
                />

                {/* Right icons */}
                <div className="absolute right-3 flex items-center gap-1">
                    {isLoading && (
                        <Loader2 className="h-4 w-4 text-gray-400 animate-spin" />
                    )}
                    {query && !isLoading && (
                        <button
                            onClick={handleClear}
                            className="text-gray-400 hover:text-gray-600"
                            aria-label="Clear search"
                        >
                            <X className="h-4 w-4" />
                        </button>
                    )}
                </div>
            </div>

            {/* Dropdown */}
            {isOpen && (
                <ul
                    id="search-suggestions"
                    role="listbox"
                    className="absolute top-full left-0 right-0 mt-1 z-50
                     bg-white border border-gray-200 rounded-lg shadow-lg
                     max-h-64 overflow-y-auto animate-fade-in"
                >
                    {suggestions.map((suggestion, index) => (
                        <li
                            key={suggestion}
                            role="option"
                            aria-selected={index === activeIndex}
                            className={`flex items-center gap-2 px-4 py-2.5 cursor-pointer
                          text-sm transition-colors
                          ${index === activeIndex
                                ? 'bg-primary-50 text-primary-700'
                                : 'text-gray-700 hover:bg-gray-50'}`}
                            onMouseEnter={() => setActiveIndex(index)}
                            onClick={() => {
                                setQuery(suggestion);
                                handleSearch(suggestion);
                            }}
                        >
                            <Search className="h-3.5 w-3.5 text-gray-400 flex-shrink-0" />
                            <span>{suggestion}</span>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}