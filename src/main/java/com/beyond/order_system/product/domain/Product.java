package com.beyond.order_system.product.domain;

import com.beyond.order_system.member.domain.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Min(value = 0)
    private int price;
    private String category;
    @Column(nullable = false)
    @Min(value = 0)
    private int stockQuantity;
    private String image_path;
    @Builder.Default
    private LocalDateTime createdTime=LocalDateTime.now();
    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;


    public void updateProductImageUrl(String productImageUrl) {
        this.image_path=productImageUrl;
    }
}
