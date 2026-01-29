package com.beyond.order_system.ordering.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderingDetailsListDto {
    private Long detailId;
    private String productName;
    private int productCount;
}
