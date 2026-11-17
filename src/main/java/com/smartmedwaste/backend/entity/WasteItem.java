package com.smartmedwaste.backend.entity;

import com.smartmedwaste.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waste_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    private WasteCategory category;

    @Column(name = "ai_confidence", precision = 5, scale = 2)
    private BigDecimal aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "colour_code")
    private ColourCode colourCode;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "tracking_id", unique = true, length = 20)
    private String trackingId;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WasteStatus status = WasteStatus.REGISTERED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "bag_count")
    private Integer bagCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}