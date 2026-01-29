package com.beyond.order_system.member.service;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dtos.MemberCreateDto;
import com.beyond.order_system.member.dtos.MemberLoginDto;
import com.beyond.order_system.member.dtos.MemberTokenDto;
import com.beyond.order_system.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Long save(MemberCreateDto dto) {
        Member member=null;
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 email입니다.");
        }
        else {
             member =memberRepository.save(dto.toEntity(passwordEncoder.encode(dto.getPassword())));
        }
        return member.getId();
    }

    public MemberTokenDto login(MemberLoginDto dto) {
        Optional<Member> opt_member = memberRepository.findByEmail(dto.getEmail());
        Member member=opt_member.orElseThrow(()->new EntityNotFoundException("잘못된 입력입니다."));
        return MemberTokenDto.fromEntity(jwtTokenProvider.createToken(member));
    }

}
