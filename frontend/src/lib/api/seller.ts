import apiClient from '@/lib/axios';
import type { Product } from '@/types/product';

export interface CreateProductPayload {
    name:           string;
    description:    string;
    price:          number;
    currencyCode:   string;
    compareAtPrice: number | null;
    categoryId:     string;
    sku:            string | null;
    brand:          string | null;
    tags:           string[];
}

export const sellerApi = {

    getMyProducts: async (params: {
        status?: string;
        page?:   number;
        size?:   number;
    }): Promise<Product[]> => {
        const { data } = await apiClient.get('/api/v1/products/my', { params });
        return data;
    },

    createProduct: async (
        payload: CreateProductPayload
    ): Promise<{ productId: string; slug: string; status: string }> => {
        const { data } = await apiClient.post('/api/v1/products', payload);
        return data;
    },

    updateProduct: async (
        id: string,
        payload: Partial<CreateProductPayload>
    ): Promise<void> => {
        await apiClient.put(`/api/v1/products/${id}`, payload);
    },

    deleteProduct: async (id: string): Promise<void> => {
        await apiClient.delete(`/api/v1/products/${id}`);
    },

    uploadImage: async (
        productId: string,
        file:      File
    ): Promise<{ imageId: string; url: string; isPrimary: boolean }> => {
        const form = new FormData();
        form.append('file', file);
        const { data } = await apiClient.post(
            `/api/v1/products/${productId}/images`,
            form,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return data;
    },

    submitForReview: async (productId: string): Promise<void> => {
        await apiClient.post(`/api/v1/products/${productId}/submit`);
    },
};