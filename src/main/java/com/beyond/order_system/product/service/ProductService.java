package com.beyond.order_system.product.service;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.dtos.ProductCreateDto;
import com.beyond.order_system.product.dtos.ProductDetailDto;
import com.beyond.order_system.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;

@Transactional
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket1}")
    private String bucket;


    public ProductService(ProductRepository productRepository, S3Client s3Client, JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.memberRepository = memberRepository;
    }

    public Long save(ProductCreateDto dto, MultipartFile productImage) {
        Optional<Product> opt_product = productRepository.findByName(dto.getName());
        if (opt_product.isPresent()) {
            throw new IllegalArgumentException("이미 있는 상품입니다.");
        } else {
            String email=SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            Member member=memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("잘못된 접근입니다."));
            Product entityProduct = dto.toEntity(member);
            Product product = productRepository.save(entityProduct);
            if (productImage != null) {
                String fileName = "user-" + product.getName() + "-productImage-" + productImage.getOriginalFilename();
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(productImage.getContentType())
                        .build();
                try {
                    s3Client.putObject(request, RequestBody.fromBytes(productImage.getBytes()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
                product.updateProductImageUrl(imgUrl);
            }
            return product.getId();
        }
    }
    @Transactional(readOnly = true)
    public ProductDetailDto findById(Long id) {
        Product product=productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 상품입니다."));
        return ProductDetailDto.fromEntity(product);
    }



}
