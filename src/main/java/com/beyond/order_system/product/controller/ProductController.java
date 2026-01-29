package com.beyond.order_system.product.controller;

import com.beyond.order_system.product.dtos.ProductCreateDto;
import com.beyond.order_system.product.service.ProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public Long productCreate(@ModelAttribute ProductCreateDto dto, @RequestParam(value = "productImage")MultipartFile productImage) {
        return productService.save(dto, productImage);
    }
}
