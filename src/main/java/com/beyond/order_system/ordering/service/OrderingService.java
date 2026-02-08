package com.beyond.order_system.ordering.service;

import com.beyond.order_system.common.service.RabbitMqStockService;
import com.beyond.order_system.common.service.SseAlarmService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitMqStockService rabbitMqStockService;

    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, RabbitMqStockService rabbitMqStockService) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.rabbitMqStockService = rabbitMqStockService;
    }

//    동시성 제어방법 1. 특정 메서드에 한해 격리수준 높이기
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long save(List<OrderingCreateDto> dtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderingCreateDto dto : dtoList) {
//            동시성 제어 방법2. select for update를 통한 락 설정 후 조회
//            Product product = productRepository.findByIdForUpdate(dto.getProductId())
//                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
//            재고 처리
//            동시성 제어방법 3. redis에서 재고수량 확인 및 재고수량 감소 처리
//            단점 : 조회와 감소 요청이 분리되다보니, 동시성 문제 발생 -> 해결책 : 루아(lua)스크립트를 통해 여러 작업을 단일 요청으로 묶어 해결.
            String remain=redisTemplate.opsForValue().get(String.valueOf(dto.getProductId()));
            int remainQuantity=Integer.parseInt(remain);
            if(remainQuantity<dto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다. 현재 "+product.getName() +"의 주문 가능 수량은 "+product.getStockQuantity()+"개입니다.");
            }
            else {
                redisTemplate.opsForValue().decrement(String.valueOf(dto.getProductId()), dto.getProductCount());
            }
//            if(product.getStockQuantity()<dto.getProductCount()) {
//                throw new IllegalArgumentException("재고가 부족합니다. 현재 "+product.getName() +"의 주문 가능 수량은 "+product.getStockQuantity()+"개입니다.");
//            }

//            product.updateStockQuantity(dto.getProductCount());
            OrderingDetails detail = OrderingDetails.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetails().add(detail);

//            rdb 동기화를 위한 작업1 : 스케줄러 활용
//            rdb 동기화를 위한 작업2 : rabbitmq에 rdb 재고감소 메시지 발행
            rabbitMqStockService.publish(dto.getProductId(), dto.getProductCount());
        }
        orderingRepository.save(ordering);
//        주문 성공 시 admin 유저에게 알림메시지 전송
        String message=ordering.getId()+"번 주문이 들어왔습니다";;
        sseAlarmService.sendMessage("admin@naver.com", email,message);
        return ordering.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> findAll() {
//       return orderingRepository.findAll().stream().map(o->OrderingListDto.builder()
//                .id(o.getId())
//                .memberEmail(o.getMember().getEmail())
//                .orderStatus(o.getOrderStatus())
//                .orderDetails(o.getOrderDetails().stream().map(od-> OrderingDetailsListDto.builder()
//                        .detailId(od.getId())
//                        .productName(od.getProduct().getName())
//                        .productCount(od.getQuantity())
//                        .build()).toList()).build()).toList();

        return orderingRepository.findAll().stream().map(o->OrderingListDto.fromEntity(o)).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> findAllMine() {
        String email=SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        return orderingRepository.findAll().stream().filter(o->o.getMember().getEmail().equals(email)).map(o->MyOrderingListDto.builder()
//                .id(o.getId())
//                .memberEmail(email)
//                .orderDetails(o.getOrderDetails().stream().map(od->OrderingDetailsListDto.builder()
//                        .detailId(od.getId())
//                        .productName(od.getProduct().getName())
//                        .productCount(od.getQuantity())
//                        .build()).toList()).orderStatus(o.getOrderStatus()).build()).toList();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderingListDto.fromEntity(o)).toList();
    }
}
