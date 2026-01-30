package com.beyond.order_system.ordering.service;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import com.beyond.order_system.member.service.MemberService;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import com.beyond.order_system.ordering.domain.Status;
import com.beyond.order_system.ordering.dtos.MyOrderingListDto;
import com.beyond.order_system.ordering.dtos.OrderingCreateDto;
import com.beyond.order_system.ordering.dtos.OrderingDetailsListDto;
import com.beyond.order_system.ordering.dtos.OrderingListDto;
import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    public Long save(List<OrderingCreateDto> dtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        Ordering ordering = Ordering.builder()
                .member(member)
                .orderStatus(Status.ordered)
                .build();

        for (OrderingCreateDto dto : dtoList) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

            OrderingDetails detail = OrderingDetails.builder()
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
//            재고 처리
            int updatedStockQuantity=product.getStockQuantity()-dto.getProductCount();
            if(updatedStockQuantity<0) {
                throw new IllegalArgumentException("재고가 부족합니다. 현재"+product.getName() +"의 주문 가능 수량은 "+product.getStockQuantity()+"개입니다.");
            }
            product.updateStockQuantity(updatedStockQuantity);
            ordering.addDetail(detail);
        }
        orderingRepository.save(ordering);
        return ordering.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> findAll() {
       return orderingRepository.findAll().stream().map(o->OrderingListDto.builder()
                .id(o.getId())
                .memberEmail(o.getMember().getEmail())
                .orderStatus(o.getOrderStatus())
                .orderDetails(o.getOrderDetails().stream().map(od-> OrderingDetailsListDto.builder()
                        .detailId(od.getId())
                        .productName(od.getProduct().getName())
                        .productCount(od.getQuantity())
                        .build()).toList()).build()).toList();
    }

    @Transactional(readOnly = true)
    public List<MyOrderingListDto> findAllMine() {
        String email=SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return orderingRepository.findAll().stream().filter(o->o.getMember().getEmail().equals(email)).map(o->MyOrderingListDto.builder()
                .id(o.getId())
                .memberEmail(email)
                .orderDetails(o.getOrderDetails().stream().map(od->OrderingDetailsListDto.builder()
                        .detailId(od.getId())
                        .productName(od.getProduct().getName())
                        .productCount(od.getQuantity())
                        .build()).toList()).orderStatus(o.getOrderStatus()).build()).toList();
    }
}
