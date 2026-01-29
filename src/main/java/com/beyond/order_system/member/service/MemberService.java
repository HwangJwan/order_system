package com.beyond.order_system.member.service;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dtos.MemberCreateDto;
import com.beyond.order_system.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
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


}
