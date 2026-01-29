package com.beyond.order_system.member.controller;

import com.beyond.order_system.member.dtos.MemberCreateDto;
import com.beyond.order_system.member.dtos.MemberLoginDto;
import com.beyond.order_system.member.dtos.MemberTokenDto;
import com.beyond.order_system.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/create")
    public Long create(@RequestBody MemberCreateDto dto) {
        return memberService.save(dto);
    }

    @PostMapping("/doLogin")
    public MemberTokenDto login(@RequestBody MemberLoginDto dto) {
        return memberService.login(dto);
    }

}
