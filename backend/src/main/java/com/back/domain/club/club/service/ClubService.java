package com.back.domain.club.club.service;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.error.ClubErrorCode;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.club.repository.ClubSpecification;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.service.ClubMemberValidService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.enums.EventType;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final MemberService memberService;
    private final ClubMemberValidService clubMemberValidService;
    private final S3Service s3Service;
    private final Rq rq;

    /**
     * 클럽 호스트 권한을 검증합니다.
     * @param clubId 클럽 ID
     * @throws ServiceException 권한이 없는 경우 403 예외 발생
     */
    public void validateHostPermission(Long clubId) {
        Member user = memberService.findMemberById(rq.getActor().getId())
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 유저를 찾을 수 없습니다."));
        if (!clubMemberValidService.checkMemberRole(clubId, user.getId(), new ClubMemberRole[]{ClubMemberRole.HOST})) {
            throw new ServiceException(403, "권한이 없습니다.");
        }
    }

    /**
     * 마지막으로 생성된 클럽을 반환합니다.
     * @return 마지막으로 생성된 클럽
     */
    public Club getLastCreatedClub() {
        return clubRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("마지막으로 생성된 클럽이 없습니다."));
    }

    /**
     * 클럽 ID로 클럽을 조회합니다.
     * @param clubId 클럽 ID
     * @return 클럽 정보
     */
    public Optional<Club> getClubById(Long clubId) {
        return clubRepository.findById(clubId);
    }

    /**
     * 모임 ID로 모임 조회
     * @param clubId 모임 ID
     * @return 모임 엔티티
     */
    public Club getClub(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException(ClubErrorCode.CLUB_NOT_FOUND.getMessage()));
    }

    /**
     * 모임 ID로 활성화된 모임 조회
     * @param clubId 모임 ID
     * @return 활성화된 모임 엔티티
     */
    public Club getActiveClub(Long clubId) {
        return clubRepository.findByIdAndStateIsTrue(clubId)
                .orElseThrow(() -> new NoSuchElementException(ClubErrorCode.CLUB_NOT_FOUND.getMessage()));
    }
    /**
     * 모임 ID로 종료일 안지난 활성화된 모임 조회
     * @param clubId 모임 ID
     * @return 활성화된 모임 엔티티
     */
    public Club getValidAndActiveClub(Long clubId) {
        return clubRepository
                .findValidAndActiveClub(clubId)
                .orElseThrow(() -> new NoSuchElementException(ClubErrorCode.CLUB_NOT_FOUND.getMessage()));
    }

    /**
     * 클럽을 생성합니다. (테스트용. controller에서 사용하지 않음)
     * @param club 클럽 정보
     * @return 생성된 클럽
     */
    @Transactional
    public Club createClub(Club club) {
        return clubRepository.save(club);
    }

    @Transactional
    public Club createClub(
            ClubControllerDtos.CreateClubRequest reqBody,
            MultipartFile image
    ) throws IOException {


        // 1. 이미지 없이 클럽 생성
        Club club = clubRepository.saveAndFlush(
                Club.builder()
                .name(reqBody.name())
                .bio(reqBody.bio())
                .category(ClubCategory.fromString(reqBody.category().toUpperCase()))
                .mainSpot(reqBody.mainSpot())
                .maximumCapacity(reqBody.maximumCapacity())
                .eventType(EventType.fromString(reqBody.eventType().toUpperCase()))
                .startDate(LocalDate.parse(reqBody.startDate()))
                .endDate(LocalDate.parse(reqBody.endDate()))
                .isPublic(reqBody.isPublic())
                .leaderId(Optional.ofNullable(rq.getActor())
                        .map(Member::getId)
                        .orElseThrow(() -> new ServiceException(401, "인증되지 않은 사용자입니다.")))
                .build()
        );
        // 2. 이미지가 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()){
            // 이미지 파일 크기 제한 (5MB)
            if (image.getSize() > (5 * 1024 * 1024)) { // 5MB
                throw new ServiceException(400, "이미지 파일 크기는 5MB를 초과할 수 없습니다.");
            }

            String imageUrl = s3Service.upload(image, "club/" + club.getId() + "/profile");
            club.updateImageUrl(imageUrl); // 클럽에 이미지 URL 설정
            clubRepository.save(club); // 이미지 URL 업데이트 후 클럽 정보 저장
        }

        // 클럽 생성 시 유저를 리더로 설정하고 멤버에 추가
        Member leader = memberService.findMemberById(rq.getActor().getId())
                .orElseThrow(() -> new NoSuchElementException("ID " + rq.getActor().getId() + "에 해당하는 리더를 찾을 수 없습니다."));
        ClubMember clubLeader = ClubMember.builder()
                .member(leader)
                .role(ClubMemberRole.HOST) // 클럽 생성자는 HOST 역할
                .state(ClubMemberState.JOINING) // 초기 상태는 JOINING으로 설정
                .build();
        club.addClubMember(clubLeader); // 연관관계 편의 메서드를 사용하여 Club에 ClubMember 추가

        // 클럽 멤버 설정
        Arrays.stream(reqBody.clubMembers()).forEach(memberInfo -> {
            // 멤버 ID로 Member 엔티티 조회
            Member member = memberService.findMemberById(memberInfo.id())
              .orElseThrow(() -> new NoSuchElementException("ID " + memberInfo.id() + "에 해당하는 멤버를 찾을 수 없습니다."));

            // ClubMember 엔티티 생성
            ClubMember clubMember = ClubMember.builder()
                    .member(member)
                    .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase())) // 문자열을 Enum으로 변환
                    .state(ClubMemberState.INVITED) // 초기 상태는 INVITED로 설정
                    .build();

            // 연관관계 편의 메서드를 사용하여 Club에 ClubMember 추가
            club.addClubMember(clubMember);
        });
        return club;
    }

    /**
     * 클럽 정보를 업데이트합니다.
     * @param clubId 클럽 ID
     * @param dto 클럽 정보 업데이트 요청 DTO
     * @param image 클럽 이미지 파일 (선택적)
     * @return 업데이트된 클럽 정보
     * @throws IOException 이미지 업로드 중 발생할 수 있는 예외
     */
    @Transactional
    public Club updateClub (Long clubId, ClubControllerDtos.@Valid UpdateClubRequest dto, MultipartFile image) throws IOException {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽을 찾을 수 없습니다."));

        // 클럽 정보 업데이트
        String name = dto.name() != null ? dto.name() : club.getName();
        String bio = dto.bio() != null ? dto.bio() : club.getBio();
        ClubCategory category = dto.category() != null ? ClubCategory.fromString(dto.category().toUpperCase()) : club.getCategory();
        String mainSpot = dto.mainSpot() != null ? dto.mainSpot() : club.getMainSpot();
        int maximumCapacity = dto.maximumCapacity() != null ? dto.maximumCapacity() : club.getMaximumCapacity();
        boolean recruitingStatus = dto.recruitingStatus() != null ? dto.recruitingStatus() : club.isRecruitingStatus();
        EventType eventType = dto.eventType() != null ? EventType.fromString(dto.eventType().toUpperCase()) : club.getEventType();
        LocalDate startDate = dto.startDate() != null ? LocalDate.parse(dto.startDate()) : club.getStartDate();
        LocalDate endDate = dto.endDate() != null ? LocalDate.parse(dto.endDate()) : club.getEndDate();
        boolean isPublic = dto.isPublic() != null ? dto.isPublic() : club.isPublic();

        club.updateInfo(name, bio, category, mainSpot, maximumCapacity, recruitingStatus, eventType, startDate, endDate, isPublic);

        // 이미지가 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()) {
            // 이미지 파일 크기 제한 (5MB)
            if (image.getSize() > (5 * 1024 * 1024)) { // 5MB
                throw new ServiceException(400, "이미지 파일 크기는 5MB를 초과할 수 없습니다.");
            }

            String imageUrl = s3Service.upload(image, "club/" + club.getId() + "/profile");
            club.updateImageUrl(imageUrl); // 클럽에 이미지 URL 설정
        }


        return clubRepository.save(club);
    }

    public void deleteClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽을 찾을 수 없습니다."));

        // 클럽 삭제
        club.changeState(false); // 클럽 상태를 비활성화로 변경
        clubRepository.save(club); // 변경된 클럽 상태를 저장
    }

    /**
     * 클럽 정보를 조회합니다.
     * @param clubId 클럽 ID
     * @return 클럽 정보 DTO
     */
    @Transactional(readOnly = true)
    public ClubControllerDtos.ClubInfoResponse getClubInfo(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽을 찾을 수 없습니다."));

        Member leader = memberService.findMemberById(club.getLeaderId())
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽 리더를 찾을 수 없습니다."));

        // 비공개 클럽인 경우, 현재 로그인한 유저가 클럽 멤버인지 확인
        if (!club.isPublic()) {
            if (rq.getActor() == null || !clubMemberValidService.isClubMember(clubId, rq.getActor().getId())) {
                throw new ServiceException(403, "비공개 클럽 정보는 클럽 멤버만 조회할 수 있습니다.");
            }
        }

        // 비활성화된 클럽인 경우 예외 처리
        if (!club.isState()) {
            throw new ServiceException(404, "해당 클럽은 비활성화 상태입니다.");
        }

        return new ClubControllerDtos.ClubInfoResponse(
                club.getId(),
                club.getName(),
                club.getBio(),
                club.getCategory().toString(),
                club.getMainSpot(),
                club.getMaximumCapacity(),
                club.isRecruitingStatus(),
                club.getEventType().toString(),
                club.getStartDate().toString(),
                club.getEndDate().toString(),
                club.isPublic(),
                club.getImageUrl(),
                club.getLeaderId(),
                leader.getNickname()
        );
    }

    /**
     * 공개 클럽 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 정보
     * @param name 클럽 이름 (선택적)
     * @param mainSpot 지역 (선택적)
     * @param category 카테고리 (선택적)
     * @param eventType 모집 유형 (선택적)
     * @return 공개 클럽 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<ClubControllerDtos.SimpleClubInfoWithoutLeader> getPublicClubs(
            Pageable pageable, String name, String mainSpot, ClubCategory category, EventType eventType
            ) {
        // 1. 기본 조건인 '공개된 클럽'으로 Specification을 시작합니다.
        Specification<Club> spec = ClubSpecification.isPublic();

        // 2. 각 필터 조건이 존재할 경우, 'and'로 연결합니다.
        if (StringUtils.hasText(name)) {
            spec = spec.and(ClubSpecification.likeName(name));
        }
        if (StringUtils.hasText(mainSpot)) {
            spec = spec.and(ClubSpecification.likeMainSpot(mainSpot));
        }
        if (category != null) {
            spec = spec.and(ClubSpecification.equalCategory(category));
        }
        if (eventType != null) {
            spec = spec.and(ClubSpecification.equalEventType(eventType));
        }

        // 3. 최종적으로 조합된 Specification과 Pageable 객체로 JpaSpecificationExecutor의 findAll을 호출합니다.
        return clubRepository.findAll(spec, pageable)
                .map(club -> new ClubControllerDtos.SimpleClubInfoWithoutLeader(
                        club.getId(),
                        club.getName(),
                        club.getCategory().toString(),
                        club.getImageUrl(),
                        club.getMainSpot(),
                        club.getEventType().toString(),
                        club.getStartDate().toString(),
                        club.getEndDate().toString(),
                        club.getBio()
                ));
    }

    public Optional<Club> findClubById(Long clubId) {
        return clubRepository.findById(clubId);
    }
}
