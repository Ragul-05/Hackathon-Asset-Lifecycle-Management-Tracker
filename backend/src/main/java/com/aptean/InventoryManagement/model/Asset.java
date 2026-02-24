package com.aptean.InventoryManagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"serialNumber"})
})
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 120)
    private String serialNumber;

    private LocalDate purchaseDate;

    private BigDecimal purchaseCost;

    @Column(length = 120)
    private String vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssetStatus status = AssetStatus.AVAILABLE;

    @Column(length = 160)
    private String location;

    private Integer usefulLifeMonths;

    private BigDecimal salvageValue;

    @Column(length = 512)
    private String imageUrl;

    @Column(length = 256)
    private String imagePublicId;

    @Column(length = 512)
    private String documentUrl;

    @Column(length = 256)
    private String documentPublicId;

    @Column(length = 512)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
