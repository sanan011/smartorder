export interface ProductImage {
    id:           string;
    url:          string;
    altText:      string;
    displayOrder: number;
    primary:      boolean;
}

export interface Product {
    id:             string;
    name:           string;
    description:    string;
    slug:           string;
    price:          number;
    currencyCode:   string;
    compareAtPrice: number | null;
    categoryId:     string;
    sellerId:       string;
    status:         string;
    sku:            string | null;
    brand:          string | null;
    averageRating:  number;
    reviewCount:    number;
    images:         ProductImage[];
    tags:           string[];
    createdAt:      string;
    updatedAt:      string;
}

export interface ProductSearchResult {
    products:   Product[];
    totalHits:  number;
    page:       number;
    size:       number;
    totalPages: number;
}

export interface Category {
    id:           string;
    name:         string;
    slug:         string;
    description:  string | null;
    parentId:     string | null;
    displayOrder: number;
    active:       boolean;
}