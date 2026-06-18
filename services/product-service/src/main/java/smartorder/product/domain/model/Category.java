package com.smartorder.product.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Category aggregate — pure Java domain object.
 * Supports a single level of nesting via parentId.
 * Example: Electronics → Mobile Phones → Smartphones
 */
public class Category {

    private final UUID   id;
    private       String name;
    private       String slug;          // URL-friendly identifier
    private       String description;
    private       UUID   parentId;      // null = top-level category
    private       int    displayOrder;
    private       boolean active;
    private final Instant createdAt;
    private       Instant updatedAt;

    // ── Creation constructor ──────────────────────────────────
    public Category(String name,
                    String slug,
                    String description,
                    UUID parentId,
                    int displayOrder) {
        this.id           = UUID.randomUUID();
        this.name         = name;
        this.slug         = slug.toLowerCase().trim();
        this.description  = description;
        this.parentId     = parentId;
        this.displayOrder = displayOrder;
        this.active       = true;
        this.createdAt    = Instant.now();
        this.updatedAt    = Instant.now();
    }

    // ── Reconstitution constructor ────────────────────────────
    public Category(UUID id, String name, String slug,
                    String description, UUID parentId,
                    int displayOrder, boolean active,
                    Instant createdAt, Instant updatedAt) {
        this.id           = id;
        this.name         = name;
        this.slug         = slug;
        this.description  = description;
        this.parentId     = parentId;
        this.displayOrder = displayOrder;
        this.active       = active;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // ── Business rules ────────────────────────────────────────
    public void rename(String newName, String newSlug) {
        this.name      = newName;
        this.slug      = newSlug.toLowerCase().trim();
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active    = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active    = true;
        this.updatedAt = Instant.now();
    }

    public boolean isTopLevel() {
        return this.parentId == null;
    }

    // ── Getters ───────────────────────────────────────────────
    public UUID    getId()           { return id; }
    public String  getName()         { return name; }
    public String  getSlug()         { return slug; }
    public String  getDescription()  { return description; }
    public UUID    getParentId()     { return parentId; }
    public int     getDisplayOrder() { return displayOrder; }
    public boolean isActive()        { return active; }
    public Instant getCreatedAt()    { return createdAt; }
    public Instant getUpdatedAt()    { return updatedAt; }
}