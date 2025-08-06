package com.back.domain.member.member.support;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MemberFixture {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final ApiKeyService apiKeyService;
    private final MemberService memberService;

    public MemberFixture(MemberRepository memberRepository, MemberInfoRepository memberInfoRepository, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.apiKeyService = new ApiKeyService();
        this.memberService = memberService;
    }

    public Member createMember(int i) {
        // 1. 회원 엔티티 생성 (비밀번호는 해시처리)
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String tag;

        do {
            tag = UUID.randomUUID().toString().substring(0, 6);
        } while (memberRepository.existsByNicknameAndTag("테스트유저" + i, tag));

        Member member = Member.createMember("테스트유저" + i,
                passwordEncoder.encode("password123"),
                tag);

        // 2. 회원정보 엔티티 생성 + 연관관계 설정
        MemberInfo memberInfo = MemberInfo.builder()
                .email("test" + i + "@example.com")
                .bio("소개입니다")
                .profileImageUrl(null)
                .apiKey(apiKeyService.generateApiKey())
                .member(member)  // MemberInfo → Member 연결
                .build();

        // 3. 양방향 연관관계 설정
        member.setMemberInfo(memberInfo);  // Member → MemberInfo 연결

        // 4. 저장 (Member 먼저 저장해야 ID가 생성됨)
        memberRepository.save(member);
        memberInfoRepository.save(memberInfo);

        return member;
    }

    public List<Member> createMultipleMember(int count) {
        List<Member> memberList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            memberList.add(createMember(i + 1));
        }
        return memberList;
    }
}
