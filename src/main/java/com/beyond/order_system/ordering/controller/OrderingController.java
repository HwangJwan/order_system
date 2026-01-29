package com.beyond.order_system.ordering.controller;

import com.beyond.order_system.ordering.dtos.OrderingCreateDto;
import com.beyond.order_system.ordering.dtos.OrderingListDto;
import com.beyond.order_system.ordering.service.OrderingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;

    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    @PostMapping("/create")
    public Long create(@RequestBody List<OrderingCreateDto> dtoList) {
        return orderingService.save(dtoList);
    }

    @GetMapping("/list")
    public List<OrderingListDto> findAll() {
        return orderingService.findAll();
    }
}
