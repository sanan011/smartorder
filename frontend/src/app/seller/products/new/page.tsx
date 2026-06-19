'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
    ArrowLeft, Upload, X, Loader2,
    Image as ImageIcon, Plus,
} from 'lucide-react';
import Link from 'next/link';
import Image from 'next/image';
import SellerSidebar from '@/components/seller/SellerSidebar';
import { sellerApi } from '@/lib/api/seller';
import { productsApi } from '@/lib/api/products';
import type { Category } from '@/types/product';
import toast from 'react-hot-toast';
import { useAuth } from '@/hooks/useAuth';

// ── Schema ────────────────────────────────────────────────
const schema = z.object({
    name:           z.string().min(1).max(255),
    description:    z.string().min(1),
    price:          z.coerce.number().min(0.01),
    currencyCode:   z.string().length(3).default('USD'),
    compareAtPrice: z.coerce.number().min(0).optional()
        .nullable(),
    categoryId:     z.string().min(1, 'Category is required'),
    sku:            z.string().max(100).optional().nullable(),
    brand:          z.string().max(150).optional().nullable(),
    tags:           z.string().optional(),
});

type FormData = z.infer<typeof schema>;

export default function NewProductPage() {
    const router = useRouter();
    const { isAuthenticated, isSeller, isAdmin } = useAuth();

    const [categories,   setCategories]   = useState<Category[]>([]);
    const [createdId,    setCreatedId]    = useState<string | null>(null);
    const [images,       setImages]       = useState<
    { file: File; preview: string; uploaded?: string }[]
    >([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isUploading,  setIsUploading]  = useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormData>({
        resolver:      zodResolver(schema),
        defaultValues: { currencyCode: 'USD' },
    });

    useEffect(() => {
        productsApi.search({ size: 100 })
            .catch(() => null);

        // Load categories from a dedicated endpoint when available
        // For now we use a placeholder list
        setCategories([
            { id: '', name: 'Select category', slug: '', description: null,
                parentId: null, displayOrder: 0, active: true },
        ]);
    }, []);

    // ── Image selection ───────────────────────────────────────
    const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(e.target.files ?? []);
        const newImages = files.map(file => ({
            file,
            preview: URL.createObjectURL(file),
        }));
        setImages(prev => [...prev, ...newImages].slice(0, 10));
        e.target.value = '';
    };

    const removeImage = (index: number) => {
        setImages(prev => {
            URL.revokeObjectURL(prev[index].preview);
            return prev.filter((_, i) => i !== index);
        });
    };

    // ── Form submit ───────────────────────────────────────────
    const onSubmit = async (data: FormData) => {
        setIsSubmitting(true);
        try {
            // 1. Create product
            const result = await sellerApi.createProduct({
                name:           data.name,
                description:    data.description,
                price:          data.price,
                currencyCode:   data.currencyCode,
                compareAtPrice: data.compareAtPrice ?? null,
                categoryId:     data.categoryId,
                sku:            data.sku ?? null,
                brand:          data.brand ?? null,
                tags:           data.tags
                    ? data.tags.split(',').map(t => t.trim()).filter(Boolean)
                    : [],
            });

            setCreatedId(result.productId);
            toast.success('Product created!');

            // 2. Upload images if any
            if (images.length > 0) {
                setIsUploading(true);
                for (const img of images) {
                    try {
                        await sellerApi.uploadImage(result.productId, img.file);
                    } catch {
                        toast.error(`Failed to upload ${img.file.name}`);
                    }
                }
                setIsUploading(false);
            }

            toast.success('Redirecting to your products...');
            router.push('/seller/products');

        } catch (err: any) {
            toast.error(
                err.response?.data?.message ?? 'Failed to create product'
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="flex gap-8">
                <SellerSidebar />

                <main className="flex-1 min-w-0">
                    {/* Header */}
                    <div className="flex items-center gap-3 mb-6">
                        <Link href="/seller/products"
                              className="p-1.5 text-gray-400 hover:text-gray-700
                             transition-colors">
                            <ArrowLeft className="h-5 w-5" />
                        </Link>
                        <h1 className="text-2xl font-bold text-gray-900">
                            New Product
                        </h1>
                    </div>

                    <form onSubmit={handleSubmit(onSubmit)}
                          className="grid grid-cols-1 lg:grid-cols-3 gap-6">

                        {/* ── Main fields ─────────────────────────────── */}
                        <div className="lg:col-span-2 space-y-6">

                            {/* Basic info */}
                            <div className="card space-y-4">
                                <h2 className="text-base font-semibold text-gray-900">
                                    Basic Information
                                </h2>

                                <div>
                                    <label className="block text-sm font-medium
                                    text-gray-700 mb-1">
                                        Product name *
                                    </label>
                                    <input
                                        type="text"
                                        className={`input ${errors.name ? 'input-error' : ''}`}
                                        placeholder="e.g. Wireless Noise-Cancelling Headphones"
                                        {...register('name')}
                                    />
                                    {errors.name && (
                                        <p className="mt-1 text-xs text-danger-500">
                                            {errors.name.message}
                                        </p>
                                    )}
                                </div>

                                <div>
                                    <label className="block text-sm font-medium
                                    text-gray-700 mb-1">
                                        Description *
                                    </label>
                                    <textarea
                                        rows={5}
                                        className={`input resize-none ${
                                            errors.description ? 'input-error' : ''}`}
                                        placeholder="Describe your product in detail..."
                                        {...register('description')}
                                    />
                                    {errors.description && (
                                        <p className="mt-1 text-xs text-danger-500">
                                            {errors.description.message}
                                        </p>
                                    )}
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium
                                      text-gray-700 mb-1">
                                            Brand
                                        </label>
                                        <input
                                            type="text"
                                            className="input"
                                            placeholder="e.g. Sony"
                                            {...register('brand')}
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium
                                      text-gray-700 mb-1">
                                            SKU
                                        </label>
                                        <input
                                            type="text"
                                            className="input"
                                            placeholder="e.g. WH-1000XM5"
                                            {...register('sku')}
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium
                                    text-gray-700 mb-1">
                                        Tags
                                        <span className="text-gray-400 font-normal ml-1">
                      (comma-separated)
                    </span>
                                    </label>
                                    <input
                                        type="text"
                                        className="input"
                                        placeholder="e.g. wireless, audio, bluetooth"
                                        {...register('tags')}
                                    />
                                </div>
                            </div>

                            {/* Images */}
                            <div className="card space-y-4">
                                <h2 className="text-base font-semibold text-gray-900">
                                    Product Images
                                </h2>
                                <p className="text-xs text-gray-500">
                                    Upload up to 10 images. First image becomes the primary.
                                    Supported: JPEG, PNG, WebP (max 5MB each).
                                </p>

                                {/* Image grid */}
                                <div className="grid grid-cols-4 gap-3">
                                    {images.map((img, index) => (
                                        <div key={index}
                                             className="relative aspect-square rounded-lg
                                    overflow-hidden bg-gray-100 group">
                                            <Image
                                                src={img.preview}
                                                alt={`Preview ${index + 1}`}
                                                fill sizes="100px"
                                                className="object-cover"
                                            />
                                            {index === 0 && (
                                                <span className="absolute top-1 left-1
                                         badge-blue text-xs">
                          Primary
                        </span>
                                            )}
                                            <button
                                                type="button"
                                                onClick={() => removeImage(index)}
                                                className="absolute top-1 right-1 p-0.5
                                   bg-black/50 text-white rounded
                                   opacity-0 group-hover:opacity-100
                                   transition-opacity"
                                            >
                                                <X className="h-3 w-3" />
                                            </button>
                                        </div>
                                    ))}

                                    {/* Upload button */}
                                    {images.length < 10 && (
                                        <label className="aspect-square rounded-lg border-2
                                      border-dashed border-gray-300
                                      flex flex-col items-center
                                      justify-center cursor-pointer
                                      hover:border-primary-400
                                      hover:bg-primary-50 transition-colors">
                                            <ImageIcon className="h-5 w-5 text-gray-400 mb-1" />
                                            <span className="text-xs text-gray-400">Add image</span>
                                            <input
                                                type="file"
                                                accept="image/jpeg,image/png,image/webp"
                                                multiple
                                                className="sr-only"
                                                onChange={handleImageSelect}
                                            />
                                        </label>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* ── Sidebar fields ───────────────────────────── */}
                        <div className="space-y-6">

                            {/* Pricing */}
                            <div className="card space-y-4">
                                <h2 className="text-base font-semibold text-gray-900">
                                    Pricing
                                </h2>

                                <div>
                                    <label className="block text-sm font-medium
                                    text-gray-700 mb-1">
                                        Price *
                                    </label>
                                    <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2
                                     text-gray-400 text-sm">$</span>
                                        <input
                                            type="number"
                                            step="0.01"
                                            min="0"
                                            className={`input pl-7 ${
                                                errors.price ? 'input-error' : ''}`}
                                            placeholder="0.00"
                                            {...register('price')}
                                        />
                                    </div>
                                    {errors.price && (
                                        <p className="mt-1 text-xs text-danger-500">
                                            {errors.price.message}
                                        </p>
                                    )}
                                </div>

                                <div>
                                    <label className="block text-sm font-medium
                                    text-gray-700 mb-1">
                                        Compare at price
                                        <span className="text-gray-400 font-normal ml-1">
                      (original)
                    </span>
                                    </label>
                                    <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2
                                     text-gray-400 text-sm">$</span>
                                        <input
                                            type="number"
                                            step="0.01"
                                            min="0"
                                            className="input pl-7"
                                            placeholder="0.00"
                                            {...register('compareAtPrice')}
                                        />
                                    </div>
                                </div>
                            </div>

                            {/* Organisation */}
                            <div className="card space-y-4">
                                <h2 className="text-base font-semibold text-gray-900">
                                    Organisation
                                </h2>

                                <div>
                                    <label className="block text-sm font-medium
                                    text-gray-700 mb-1">
                                        Category *
                                    </label>
                                    <select
                                        className={`input ${
                                            errors.categoryId ? 'input-error' : ''}`}
                                        {...register('categoryId')}
                                    >
                                        <option value="">Select a category</option>
                                        {categories.map(cat => (
                                            <option key={cat.id} value={cat.id}>
                                                {cat.name}
                                            </option>
                                        ))}
                                    </select>
                                    {errors.categoryId && (
                                        <p className="mt-1 text-xs text-danger-500">
                                            {errors.categoryId.message}
                                        </p>
                                    )}
                                </div>
                            </div>

                            {/* Submit */}
                            <button
                                type="submit"
                                disabled={isSubmitting || isUploading}
                                className="btn-primary w-full py-3"
                            >
                                {isSubmitting || isUploading ? (
                                    <>
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                        {isUploading ? 'Uploading images...' : 'Creating...'}
                                    </>
                                ) : (
                                    <>
                                        <Plus className="h-4 w-4" />
                                        Create product
                                    </>
                                )}
                            </button>

                            <p className="text-xs text-gray-400 text-center">
                                Product will be saved as a draft. Submit for review
                                from your products list to make it live.
                            </p>
                        </div>
                    </form>
                </main>
            </div>
        </div>
    );
}