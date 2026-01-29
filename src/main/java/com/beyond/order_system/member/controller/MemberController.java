package com.beyond.order_system.member.controller;

import com.beyond.order_system.member.dtos.*;
import com.beyond.order_system.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberListDto> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/myinfo")
    @PreAuthorize("hasRole('USER')")
    public MemberMyInfoDto findMyInfo() {
        return memberService.findMyInfo();
    }
}
