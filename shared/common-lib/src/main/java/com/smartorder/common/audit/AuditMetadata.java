package com.smartorder.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Embeddable
public class AuditMetadata {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // 🎯 Lombok @Setter vasitəsilə lazımsız setter kodlarını sildik
    @Setter
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @Setter
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // 💡 Metodların görünüşünü paket daxili (package-private) etdik və IDE xəbərdarlığını dondurduq
    @PrePersist
    void onPrePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Convenience factory — used when the actor is known at construction time.
     */
    @SuppressWarnings("unused") // Əgər hələ heç bir servisdə çağırmayıbansa IDE sarı rəng etməsin deyə
    public static AuditMetadata createdBy(String actor) {
        AuditMetadata meta = new AuditMetadata();
        meta.createdBy = actor;
        meta.updatedBy = actor;
        return meta;
    }
}