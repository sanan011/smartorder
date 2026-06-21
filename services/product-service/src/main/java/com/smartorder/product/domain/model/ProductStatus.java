package com.smartorder.product.domain.model;

/**
 * Lifecycle states of a Product listing.
 *
 * DRAFT       — saved by seller, not yet submitted for review
 * PENDING     — submitted, awaiting admin approval
 * ACTIVE      — live and visible to customers
 * INACTIVE    — temporarily hidden by seller
 * REJECTED    — failed admin review (reason stored in product)
 * DELETED     — soft-deleted, retained for order history
 */
public enum ProductStatus {
    DRAFT,
    PENDING,
    ACTIVE,
    INACTIVE,
    REJECTED,
    DELETED
}