package com.beyond.order_system.product.controller;

import com.beyond.order_system.product.dtos.ProductCreateDto;
import com.beyond.order_system.product.dtos.ProductDetailDto;
import com.beyond.order_system.product.dtos.ProductListDto;
import com.beyond.order_system.product.dtos.ProductSearchDto;
import com.beyond.order_system.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Long productCreate(@ModelAttribute ProductCreateDto dto, @RequestParam(value = "productImage")MultipartFile productImage) {
        return productService.save(dto, productImage);
    }

    @GetMapping("/detail/{id}")
    public ProductDetailDto findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/list")
    public Page<ProductListDto> findAll(@PageableDefault(size=10, sort="id", direction = Sort.Direction.ASC) Pageable pageable, @ModelAttribute ProductSearchDto searchDto) {
        return productService.findAll(pageable, searchDto);
    }
}
