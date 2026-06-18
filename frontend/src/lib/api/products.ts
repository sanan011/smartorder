import apiClient from '@/lib/axios';
import type { Product, ProductSearchResult, Category } from '@/types/product';

export const productsApi = {

    getById: async (id: string): Promise<Product> => {
        const { data } = await apiClient.get(`/api/v1/products/${id}`);
        return data;
    },

    getBySlug: async (slug: string): Promise<Product> => {
        const { data } = await apiClient.get(`/api/v1/products/slug/${slug}`);
        return data;
    },

    list: async (params: {
        category?: string;
        page?: number;
        size?: number;
    }): Promise<Product[]> => {
        const { data } = await apiClient.get('/api/v1/products', { params });
        return data;
    },

    search: async (params: {
        q?:        string;
        category?: string;
        minPrice?: number;
        maxPrice?: number;
        sortBy?:   string;
        page?:     number;
        size?:     number;
    }): Promise<ProductSearchResult> => {
        const { data } = await apiClient.get('/api/v1/products/search', { params });
        return data;
    },

    autocomplete: async (prefix: string): Promise<string[]> => {
        const { data } = await apiClient.get('/api/v1/products/autocomplete', {
            params: { prefix },
        });
        return data;
    },
};