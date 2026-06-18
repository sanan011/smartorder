import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { cartApi } from '@/lib/api/cart';
import type { Cart, CartItem, AddToCartPayload } from '@/types/cart';

interface CartState {
    cart:          Cart | null;
    isOpen:        boolean;
    isLoading:     boolean;
    error:         string | null;

    // Actions
    fetchCart:     (isGuest: boolean) => Promise<void>;
    addItem:       (payload: AddToCartPayload, isGuest: boolean) => Promise<void>;
    updateItem:    (productId: string, quantity: number, isGuest: boolean) => Promise<void>;
    removeItem:    (productId: string, isGuest: boolean) => Promise<void>;
    clearCart:     (isGuest: boolean) => Promise<void>;
    mergeOnLogin:  () => Promise<void>;
    openDrawer:    () => void;
    closeDrawer:   () => void;
    toggleDrawer:  () => void;
    clearError:    () => void;

    // Computed helpers
    itemCount:     () => number;
    hasItem:       (productId: string) => boolean;
    getItem:       (productId: string) => CartItem | undefined;
}

export const useCartStore = create<CartState>()(
    persist(
        (set, get) => ({
            cart:      null,
            isOpen:    false,
            isLoading: false,
            error:     null,

            // ── Fetch ───────────────────────────────────────────────
            fetchCart: async (isGuest) => {
                set({ isLoading: true, error: null });
                try {
                    const cart = await cartApi.getCart(isGuest);
                    set({ cart, isLoading: false });
                } catch (err: any) {
                    set({
                        error:     err.response?.data?.message ?? 'Failed to load cart.',
                        isLoading: false,
                    });
                }
            },

            // ── Add item ────────────────────────────────────────────
            addItem: async (payload, isGuest) => {
                set({ isLoading: true, error: null });
                try {
                    const cart = await cartApi.addItem(payload, isGuest);
                    set({ cart, isLoading: false, isOpen: true });
                } catch (err: any) {
                    set({
                        error:     err.response?.data?.message ?? 'Failed to add item.',
                        isLoading: false,
                    });
                    throw err;
                }
            },

            // ── Update quantity ──────────────────────────────────────
            updateItem: async (productId, quantity, isGuest) => {
                set({ isLoading: true, error: null });
                try {
                    const cart = await cartApi.updateItem(productId, quantity, isGuest);
                    set({ cart, isLoading: false });
                } catch (err: any) {
                    set({
                        error:     err.response?.data?.message ?? 'Failed to update item.',
                        isLoading: false,
                    });
                }
            },

            // ── Remove item ──────────────────────────────────────────
            removeItem: async (productId, isGuest) => {
                set({ isLoading: true, error: null });
                try {
                    const cart = await cartApi.removeItem(productId, isGuest);
                    set({ cart, isLoading: false });
                } catch (err: any) {
                    set({
                        error:     err.response?.data?.message ?? 'Failed to remove item.',
                        isLoading: false,
                    });
                }
            },

            // ── Clear cart ───────────────────────────────────────────
            clearCart: async (isGuest) => {
                set({ isLoading: true });
                try {
                    await cartApi.clearCart(isGuest);
                    set({ cart: null, isLoading: false });
                } catch {
                    set({ isLoading: false });
                }
            },

            // ── Merge guest cart on login ────────────────────────────
            mergeOnLogin: async () => {
                const guestCartId = typeof window !== 'undefined'
                    ? localStorage.getItem('so_guest_cart_id')
                    : null;

                if (!guestCartId) return;

                try {
                    const merged = await cartApi.mergeGuestCart(guestCartId);
                    set({ cart: merged });
                    localStorage.removeItem('so_guest_cart_id');
                } catch (err) {
                    // Non-critical: log and continue
                    console.warn('Guest cart merge failed:', err);
                }
            },

            // ── Drawer ───────────────────────────────────────────────
            openDrawer:   () => set({ isOpen: true }),
            closeDrawer:  () => set({ isOpen: false }),
            toggleDrawer: () => set(s => ({ isOpen: !s.isOpen })),
            clearError:   () => set({ error: null }),

            // ── Computed ─────────────────────────────────────────────
            itemCount: () => get().cart?.totalItemCount ?? 0,

            hasItem: (productId) =>
                !!get().cart?.items.some(i => i.productId === productId),

            getItem: (productId) =>
                get().cart?.items.find(i => i.productId === productId),
        }),

        {
            name:    'smartorder-cart',
            storage: createJSONStorage(() =>
                typeof window !== 'undefined' ? localStorage : {
                    getItem:    () => null,
                    setItem:    () => {},
                    removeItem: () => {},
                }
            ),
            // Only persist cart data — not loading/error state
            partialize: (state) => ({
                cart:   state.cart,
                isOpen: false,
            }),
        }
    )
);