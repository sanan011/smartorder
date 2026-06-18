export interface CartItem {
    productId:       string;
    productName:     string;
    productSlug:     string;
    primaryImageUrl: string | null;
    unitPrice:       number;
    currencyCode:    string;
    quantity:        number;
    lineTotal:       number;
    sellerId:        string;
}

export interface Cart {
    cartId:         string;
    guest:          boolean;
    items:          CartItem[];
    totalItemCount: number;
    subtotal:       number;
    discountAmount: number;
    total:          number;
    couponCode:     string | null;
    currencyCode:   string;
}

export interface AddToCartPayload {
    productId:       string;
    productName:     string;
    productSlug:     string;
    primaryImageUrl: string | null;
    unitPrice:       number;
    currencyCode:    string;
    quantity:        number;
    sellerId:        string;
}