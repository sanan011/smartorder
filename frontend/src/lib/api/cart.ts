import apiClient from '@/lib/axios';
import type { Cart, AddToCartPayload } from '@/types/cart';

const GUEST_CART_HEADER = 'X-Guest-Cart-Id';

function getGuestCartId(): string {
    if (typeof window === 'undefined') return '';
    let id = localStorage.getItem('so_guest_cart_id');
    if (!id) {
        id = crypto.randomUUID();
        localStorage.setItem('so_guest_cart_id', id);
    }
    return id;
}

function cartHeaders(isGuest: boolean): Record<string, string> {
    if (isGuest) {
        return { [GUEST_CART_HEADER]: getGuestCartId() };
    }
    return {};
}

export const cartApi = {

    getCart: async (isGuest: boolean): Promise<Cart> => {
        const { data } = await apiClient.get('/api/v1/cart', {
            headers: cartHeaders(isGuest),
        });
        return data;
    },

    addItem: async (
        payload: AddToCartPayload,
        isGuest: boolean
    ): Promise<Cart> => {
        const { data } = await apiClient.post('/api/v1/cart/items', payload, {
            headers: cartHeaders(isGuest),
        });
        return data;
    },

    updateItem: async (
        productId: string,
        quantity: number,
        isGuest: boolean
    ): Promise<Cart> => {
        const { data } = await apiClient.patch(
            `/api/v1/cart/items/${productId}`,
            { quantity },
            { headers: cartHeaders(isGuest) }
        );
        return data;
    },

    removeItem: async (
        productId: string,
        isGuest: boolean
    ): Promise<Cart> => {
        const { data } = await apiClient.delete(
            `/api/v1/cart/items/${productId}`,
            { headers: cartHeaders(isGuest) }
        );
        return data;
    },

    clearCart: async (isGuest: boolean): Promise<void> => {
        await apiClient.delete('/api/v1/cart', {
            headers: cartHeaders(isGuest),
        });
    },

    mergeGuestCart: async (guestCartId: string): Promise<Cart> => {
        const { data } = await apiClient.post('/api/v1/cart/merge', {
            guestCartId,
        });
        return data;
    },
};