package com.smartorder.auth.domain.model;

/**
 * RBAC roles for the SmartOrder platform.
 *
 * CUSTOMER — standard buyer account
 * SELLER   — verified merchant who can list products
 * ADMIN    — platform administrator with full access
 * SUPPORT  — read-only customer support agent
 */
public enum Role {
    CUSTOMER,
    SELLER,
    ADMIN,
    SUPPORT
}