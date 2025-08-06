package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.dto.request.GuestDto;
import com.back.domain.member.member.dto.request.MemberLoginDto;
import com.back.domain.member.member.dto.request.MemberRegisterDto;
import com.back.domain.member.member.dto.request.UpdateMemberInfoDto;
import com.back.domain.member.member.dto.response.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final ApiKeyService apiKeyService;
    private final AuthService authService;
    private final S3Service s3Service;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ==================================================
     * 회원 관련 API 에서 사용되는 메인 메서드들 입니다.
     * - 회원가입 / 로그인 / 로그아웃 / 탈퇴 / 정보 조회 및 수정
     */

    // ============================== [회원] 회원가입 ==============================

    //[회원] 회원가입 메인 메소드
    @Transactional
    public MemberAuthResponse registerMember(MemberRegisterDto dto) {
        // 1. 이메일 중복 확인
        validateDuplicateMember(dto);

        // 2. 태그 및 API 키 생성
        String tag = generateMemberTag(dto.nickname());
        String apiKey = apiKeyService.generateApiKey();

        // 3. 멤버 및 멤버인포 DB 저장
        Member member = createAndSaveMember(dto, tag);
        createAndSaveMemberInfo(dto, member, apiKey);

        // 4. Access Token 생성 및 응답
        String accessToken = generateAccessToken(member);
        return new MemberAuthResponse(apiKey, accessToken);
    }

    // ============================== [비회원] 모임 가입 ==============================

    //[비회원] 모임 가입 메인 메소드
    @Transactional
    public GuestResponse registerGuestMember(@Valid GuestDto dto) {
        // 1. 해당 그룹 내 비회원 닉네임 중복 확인
        validateDuplicateGuest(dto);

        // 2. 태그 생성 및 비회원 DB 저장
        String tag = generateMemberTag(dto.nickname());
        Member guest = createAndSaveGuestMember(dto, tag);

        // 3. 클럽 조회
        Club club = clubRepository.findById(dto.clubId())
                .orElseThrow(() -> new ServiceException(400, "클럽을 찾을 수 없습니다."));

        // 4. ClubMember 엔티티 생성 및 저장
        ClubMember clubMember = ClubMember.builder()
                .member(guest)
                .club(club)
                .role(ClubMemberRole.PARTICIPANT)
                .state(ClubMemberState.APPLYING)
                .build();

        clubMemberRepository.save(clubMember);

        // 5. Access Token 생성 및 응답
        String accessToken = generateAccessToken(guest);
        return new GuestResponse(dto.nickname(), accessToken, dto.clubId());
    }

    // ============================== [회원] 로그인 ==============================

    //[회원] 로그인 메인 메소드
    public MemberAuthResponse loginMember(@Valid MemberLoginDto memberLoginDto) {
        // 1. 이메일로 회원 정보 조회 및 검증
        Optional<MemberInfo> memberInfo = memberInfoRepository.findByEmail(memberLoginDto.email());
        Member member = validateMemberLogin(memberInfo);

        // 2. 비밀번호 검증
        validatePassword(memberLoginDto.password(), member);

        // 3. API 키 및 Access Token 생성
        String apiKey = member.getMemberInfo().getApiKey();
        String accessToken = authService.generateAccessToken(member);

        // 4. 응답 반환
        return new MemberAuthResponse(apiKey, accessToken);
    }

    // ============================== [비회원] 임시 로그인 ==============================

    //비회원 임시 로그인 메인 메소드
    public GuestResponse loginGuestMember(@Valid GuestDto guestDto) {
        // 1. 닉네임과 클럽 ID로 비회원 조회 및 검증
        Optional<Member> optionalMember = memberRepository.findByGuestNicknameInClub(guestDto.nickname(), guestDto.clubId());
        Member member = validateGuestLogin(optionalMember);

        // 2. 비밀번호 검증
        validatePassword(guestDto.password(), member);

        // 3. Access Token 생성 및 응답
        String accessToken = authService.generateAccessToken(member);
        String nickname = guestDto.nickname();
        Long clubId = guestDto.clubId();

        return new GuestResponse(nickname, accessToken, clubId);
    }

    // ============================== [회원] 탈퇴 ==============================

    //회원 탈퇴 메인 메소드
    @Transactional
    public MemberWithdrawMembershipResponse withdrawMember(String nickname, String tag) {
        // 1. 닉네임과 태그로 회원 조회
        Member member = findMemberByNicknameAndTag(nickname, tag);
        MemberInfo memberInfo = member.getMemberInfo();

        // 2. 회원 삭제 처리
        deleteMember(member);

        // 3. 탈퇴 응답 반환
        return new MemberWithdrawMembershipResponse(member.getNickname(), member.getTag());
    }

    // ============================== [회원] 정보 조회/수정 ==============================

    //유저 정보 반환 메소드
    public MemberDetailInfoResponse getMemberInfo(Long id) {
        // 1. 회원 조회
        Member member = findMemberById(id)
                .orElseThrow(() -> new ServiceException(400, "해당 id의 유저가 없습니다."));
        MemberInfo memberInfo = member.getMemberInfo();

        // 2. 정보 추출 후 응답 DTO 생성
        String nickname = member.getNickname();
        String tag = member.getTag();
        String email = memberInfo.getEmail();
        String bio = memberInfo.getBio();
        String profileImage = memberInfo.getProfileImageUrl();

        return new MemberDetailInfoResponse(nickname, email, bio, profileImage, tag);
    }

    //유저 정보 수정 메소드
    @Transactional
    public MemberDetailInfoResponse updateMemberInfo(Long id, UpdateMemberInfoDto dto, MultipartFile image) {
        // 1. 회원 조회
        Member member = findMemberById(id).orElseThrow(() ->
                new ServiceException(400, "해당 id의 유저가 없습니다."));
        MemberInfo memberInfo = member.getMemberInfo();

        // 2. 비밀번호 변경 시 암호화 처리
        String password = member.getPassword();
        if (dto.password() != null && !dto.password().isBlank()) {
            password = passwordEncoder.encode(dto.password());
        }

        // 3. 닉네임, 태그, 바이오 등 변경 정보 설정 (기본값 유지 포함)
        String nickname = (dto.nickname() != null) ? dto.nickname() : member.getNickname();
        String tag = (dto.nickname() != null) ? generateMemberTag(dto.nickname()) : member.getTag();
        String bio = (dto.bio() != null) ? dto.bio() : memberInfo.getBio();

        // 4. 멤버 및 멤버인포 정보 업데이트
        member.updateInfo(nickname, tag, password);
        memberInfo.updateBio(bio);

        // 5. 프로필 이미지가 있을 경우 S3 업로드 처리
        if (image != null && !image.isEmpty()){
            // 5-1. 파일 형식 검증
            String contentType = image.getContentType();
            if(!contentType.startsWith("image/")){
                throw new ServiceException(400, "이미지 파일만 업로드 가능합니다.");
            }

            // 5-2. (TODO) 파일 크기 검증

            try {
                String imageUrl = s3Service.upload(image, "member/" + memberInfo.getId() + "/profile");
                memberInfo.updateImageUrl(imageUrl);
            } catch (IOException e) {
                throw new ServiceException(400, "이미지 업로드 중 오류가 발생했습니다.");
            }
        }

        // 6. 변경된 정보로 응답 DTO 생성
        return new MemberDetailInfoResponse(member.getNickname(),
                memberInfo.getEmail(),
                memberInfo.getBio(),
                memberInfo.getProfileImageUrl(),
                member.getTag());
    }

    // ============================== [검증 메소드] ==============================

    private void validateDuplicateMember(MemberRegisterDto dto) {
        // 1. 이메일 중복 확인 (소문자 변환 후)
        String email = dto.email().toLowerCase();
        if (memberInfoRepository.findByEmail(email).isPresent()) {
            throw new ServiceException(400, "이미 사용 중인 이메일입니다.");
        }
    }

    private void validateDuplicateGuest(@Valid GuestDto dto) {
        // 1. 비회원용 닉네임 중복 확인
        String nickname = dto.nickname();

        if (memberRepository.existsGuestNicknameInClub(nickname, dto.clubId())) {
            throw new ServiceException(400, "이미 사용 중인 닉네임입니다.");
        }
    }

    private void validatePassword(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ServiceException(400, "해당 사용자를 찾을 수 없습니다.");
        }
    }

    private Member validateMemberLogin(Optional<MemberInfo> memberInfo) {
        if (memberInfo.isEmpty()) {
            throw new ServiceException(400, "해당 사용자를 찾을 수 없습니다.");
        }

        return memberInfo.get().getMember();
    }

    private Member validateGuestLogin(Optional<Member> member) {
        // 1. 닉네임 기반 비회원 조회 여부 확인
        if (member.isEmpty()) {
            throw new ServiceException(400, "해당 사용자를 찾을 수 없습니다.");
        }

        return member.get();
    }

    // ============================== [생성 메소드] ==============================

    private String generateMemberTag(String nickname) {
        // 1. 태그 생성 (6자리 UUID 서브스트링, 중복 체크 반복)
        String tag;
        do {
            tag = UUID.randomUUID().toString().substring(0, 6);
        } while (memberRepository.existsByNicknameAndTag(nickname, tag));

        return tag;
    }

    private Member createAndSaveMember(MemberRegisterDto dto, String tag) {
        // 1. 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(dto.password());

        // 2. Member 엔티티 생성 및 저장
        Member member = Member.createMember(dto.nickname(), hashedPassword, tag);
        return memberRepository.save(member);
    }

    private Member createAndSaveGuestMember(@Valid GuestDto dto, String tag) {
        // 1. 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(dto.password());

        // 2. 비회원 Member 엔티티 생성 및 저장
        Member guest = Member.createGuest(dto.nickname(), hashedPassword, tag);
        return memberRepository.save(guest);
    }

    private MemberInfo createAndSaveMemberInfo(MemberRegisterDto dto, Member member, String apiKey) {
        // 1. MemberInfo 엔티티 생성 및 저장
        MemberInfo info = MemberInfo.builder()
                .email(dto.email())
                .bio(dto.bio())
                .profileImageUrl("")
                .member(member)
                .apiKey(apiKey)
                .build();

        MemberInfo savedInfo = memberInfoRepository.save(info);

        // 2. Member와 연관관계 설정
        member.setMemberInfo(savedInfo);

        return savedInfo;
    }

    // ============================== [유틸 / 기타] ==============================

    public MemberPasswordResponse checkPasswordValidity(Long memberId, String password) {
        // 1. 회원 조회
        Member member = findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(400, "해당 id로 유저를 찾을 수 없습니다."));

        // 2. 비밀번호 검증 결과 반환
        try {
            validatePassword(password, member);
            return new MemberPasswordResponse(true);
        } catch (ServiceException e) {
            return new MemberPasswordResponse(false);
        }
    }

    public Map<String, Object> payload(String accessToken) {
        // 토큰 파싱
        return authService.payload(accessToken);
    }

    public Member findMemberByEmail(String email) {
        // 1. 이메일로 MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(400, "사용자를 찾을 수 없습니다."));

        return memberInfo.getMember();
    }

    private void deleteMember(Member member) {
        // 멤버 삭제 처리
        memberRepository.delete(member);
    }

    public Member findMemberByNicknameAndTag(String nickname, String tag) {
        return memberRepository.findByNicknameAndTag(nickname, tag)
                .orElseThrow(() ->  new ServiceException(400, "회원 정보를 찾을 수 없습니다."));
    }

    public Optional<Member> findMemberById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * 멤버 ID로 멤버 조회
     * @param memberId 멤버 ID
     * @return 멤버 엔티티
     */
    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("멤버가 존재하지 않습니다."));
    }

    public String generateAccessToken(Member member) {
        return authService.generateAccessToken(member);
    }

    public Member findMemberByApiKey(String apiKey) {
        // 1. API 키로 MemberInfo 조회
        MemberInfo optionalMemberInfo = memberInfoRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException(400, "유효하지 않은 Refresh Token 입니다."));

        return optionalMemberInfo.getMember();
    }
}
