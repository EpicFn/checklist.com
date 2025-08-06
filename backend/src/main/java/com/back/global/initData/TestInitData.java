package com.back.global.initData;

import com.back.domain.checkList.checkList.entity.CheckList;
import com.back.domain.checkList.checkList.entity.CheckListItem;
import com.back.domain.checkList.checkList.repository.CheckListItemRepository;
import com.back.domain.checkList.checkList.repository.CheckListRepository;
import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.checkList.itemAssign.repository.ItemAssignRepository;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.friend.repository.FriendRepository;
import com.back.global.enums.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


/**
 * 테스트 환경의 초기 데이터 설정
 */
@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestInitData {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final FriendRepository friendRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final CheckListRepository checkListRepository;
    private final CheckListItemRepository checkListItemRepository;
    private final ItemAssignRepository itemAssignRepository;

    @Autowired
    @Lazy
    private TestInitData self;

    private Map<String, Member> members;
    private Map<String, Club> clubs;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            // 회원 관련 데이터 초기화
            self.initMemberTestData();
            self.initFriendTestData();

            // 모임 관련 데이터 초기화
            self.initGroupTestData();
            self.initGroupMemberTestData();

            // 일정 관련 데이터 초기화
            self.initScheduleTestData();

            // 체크리스트 관련 데이터 초기화
            self.initCheckListTestData();
            self.initCheckListItemTestData();
            self.initItemAssignTestData();
        };
    }

    /**
     * 회원, 회원 정보 초기 데이터 설정
     */
    @Transactional
    public void initMemberTestData() {
        members = new HashMap<>();

        // 회원
        Member member1 = createMember("홍길동", "password1", "hgd222@test.com", "안녕하세요. 홍길동입니다.");
        members.put(member1.getNickname(), member1);

        Member member2 = createMember("김철수", "password2", "chs4s@test.com", "안녕하세요. 김철수입니다.");
        members.put(member2.getNickname(), member2);

        Member member3 = createMember("이영희", "password3", "lyh3@test.com", "안녕하세요. 이영희입니다.");
        members.put(member3.getNickname(), member3);

        Member member4 = createMember("최지우", "password4", "cjw5@test.com", "안녕하세요. 최지우입니다.");
        members.put(member4.getNickname(), member4);

        Member member5 = createMember("박민수", "password5", "pms4@test.com", "안녕하세요. 박민수입니다.");
        members.put(member5.getNickname(), member5);

        Member member6 = createMember("유나영", "password6", "uny@test.com", "안녕하세요, 유나영입니다."); //가입 신청 테스트용
        members.put(member6.getNickname(), member6);

        Member member7 = createMember("이채원", "password7", "lcw@test.com", "안녕하세요, 이채원입니다."); //가입 신청 테스트용
        members.put(member7.getNickname(), member7);

        Member member8 = createMember("호윤호", "password8", "hyh@test.com", "안녕하세요, 호윤호입니다."); //가입 신청 테스트용
        members.put(member8.getNickname(), member8);

        // 비회원
        Member guest1 = createMember("이덕혜", "password11", null, null);
        members.put(guest1.getNickname(), guest1);

        Member guest2 = createMember("레베카", "password12", null, null);
        members.put(guest2.getNickname(), guest2);

        Member guest3 = createEncodedMember("김암호", "password13", null, null);
        members.put(guest3.getNickname(), guest3);
    }

    /**
     * 친구 초기 데이터 설정
     */
    @Transactional
    public void initFriendTestData() {
        Member requester = members.get("홍길동");

        // 친구 요청을 보낸 회원
        Member responder1 = members.get("이영희");
        Friend friend1 = Friend.builder()
                .requestedBy(requester)
                .member1(requester)
                .member2(responder1)
                .status(FriendStatus.PENDING)
                .build();
        friendRepository.save(friend1);

        // 친구 요청을 수락한 회원
        Member responder2 = members.get("최지우");
        Friend friend2 = Friend.builder()
                .requestedBy(requester)
                .member1(requester)
                .member2(responder2)
                .status(FriendStatus.ACCEPTED)
                .build();
        friendRepository.save(friend2);

        // 친구 요청을 거절한 회원
        Member responder3 = members.get("박민수");
        Friend friend3 = Friend.builder()
                .requestedBy(requester)
                .member1(requester)
                .member2(responder3)
                .status(FriendStatus.REJECTED)
                .build();
        friendRepository.save(friend3);
    }

    /**
     * 모임 초기 데이터 설정
     */
    @Transactional
    public void initGroupTestData() {
        Member leader1 = members.get("홍길동");
        clubs = new HashMap<>();

        // 장기 공개 모임 - 모집 중
        Club club1 = Club.builder()
                .name("산책 모임")
                .category(ClubCategory.SPORTS)
                .mainSpot("서울")
                .maximumCapacity(25)
                .recruitingStatus(true)
                .eventType(EventType.LONG_TERM)
                .startDate(LocalDate.parse("2025-07-05"))
                .endDate(LocalDate.parse("2025-08-30"))
                .isPublic(true)
                .leaderId(leader1.getId())
                .state(true).build();
        clubRepository.save(club1);
        clubs.put(club1.getName(), club1);

        ClubMember clubMember1 = ClubMember.builder()
                .member(leader1)
                .club(club1)
                .role(ClubMemberRole.HOST)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(clubMember1);

        // 장기 비공개 모임 - 모집 마감
        Club club2 = Club.builder()
                .name("친구 모임")
                .category(ClubCategory.TRAVEL)
                .mainSpot("강원도")
                .maximumCapacity(4)
                .recruitingStatus(false)
                .eventType(EventType.LONG_TERM)
                .startDate(LocalDate.parse("2025-05-01"))
                .endDate(LocalDate.parse("2026-12-31"))
                .isPublic(false)
                .leaderId(leader1.getId())
                .state(true).build();
        clubRepository.save(club2);
        clubs.put(club2.getName(), club2);

        ClubMember clubMember2 = ClubMember.builder()
                .member(leader1)
                .club(club2)
                .role(ClubMemberRole.HOST)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(clubMember2);

        // 단기 비공개 모임 - 모집중
        Club club3 = Club.builder()
                .name("친구 모임2")
                .category(ClubCategory.TRAVEL)
                .mainSpot("제주도")
                .maximumCapacity(5)
                .recruitingStatus(true)
                .eventType(EventType.SHORT_TERM)
                .startDate(LocalDate.parse("2025-07-01"))
                .endDate(LocalDate.parse("2025-12-31"))
                .isPublic(false)
                .leaderId(leader1.getId())
                .state(true).build();
        clubRepository.save(club3);
        clubs.put(club3.getName(), club3);

        ClubMember clubMember3 = ClubMember.builder()
                .member(leader1)
                .club(club3)
                .role(ClubMemberRole.HOST)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(clubMember3);

        Member leader2 = members.get("최지우");

        // 일회성 공개 모임 - 모집 중
        Club club4 = Club.builder()
                .name("A도시 러닝 대회")
                .category(ClubCategory.SPORTS)
                .mainSpot("서울")
                .maximumCapacity(50)
                .recruitingStatus(true)
                .eventType(EventType.ONE_TIME)
                .startDate(LocalDate.parse("2025-08-10"))
                .endDate(LocalDate.parse("2025-08-10"))
                .isPublic(true)
                .leaderId(leader2.getId())
                .state(true).build();
        clubRepository.save(club4);
        clubs.put(club4.getName(), club4);

        ClubMember clubMember4 = ClubMember.builder()
                .member(leader2)
                .club(club4)
                .role(ClubMemberRole.HOST)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(clubMember4);

        // 종료일 지난 모임
        Club nClub1 = Club.builder()
                .name("독서 모임")
                .category(ClubCategory.STUDY)
                .mainSpot("부산")
                .maximumCapacity(10)
                .recruitingStatus(true)
                .eventType(EventType.SHORT_TERM)
                .startDate(LocalDate.parse("2025-07-12"))
                .endDate(LocalDate.parse("2025-07-12"))
                .imageUrl("img3")
                .isPublic(false)
                .leaderId(leader2.getId())
                .state(true).build();
        clubRepository.save(nClub1);
        clubs.put(nClub1.getName(), nClub1);

        ClubMember nClubMember1 = ClubMember.builder()
                .member(leader2)
                .club(nClub1)
                .role(ClubMemberRole.HOST)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(nClubMember1);

        // 삭제된 모임
        Club nClub2 = Club.builder()
                .name("테니스 모임")
                .category(ClubCategory.SPORTS)
                .mainSpot("충청도 A 테니스장")
                .maximumCapacity(2)
                .recruitingStatus(false)
                .eventType(EventType.SHORT_TERM)
                .startDate(LocalDate.parse("2025-07-05"))
                .endDate(LocalDate.parse("2025-08-11"))
                .imageUrl("img4")
                .isPublic(false)
                .leaderId(leader1.getId())
                .state(false).build();
        clubRepository.save(nClub2);
        clubs.put(nClub2.getName(), nClub2);

        ClubMember nClubMember2 = ClubMember.builder()
                .member(leader2)
                .club(nClub2)
                .role(ClubMemberRole.HOST)
                .state(ClubMemberState.JOINING)
                .build();
        clubMemberRepository.save(nClubMember2);
    }

    /**
     * 모임 맴버 헬퍼 dto
     */
    private record GroupMemberData(
            String clubName,
            String memberNickname,
            ClubMemberRole role
    ) {
    }

    /**
     * 모임 맴버 초기 데이터 설정
     */
    @Transactional
    public void initGroupMemberTestData() {
        List<GroupMemberData> groupMembers = List.of(
                new GroupMemberData("산책 모임", "김철수", ClubMemberRole.MANAGER),
                new GroupMemberData("산책 모임", "이영희", ClubMemberRole.PARTICIPANT),
                new GroupMemberData("친구 모임", "박민수", ClubMemberRole.PARTICIPANT),
                new GroupMemberData("친구 모임", "이영희", ClubMemberRole.PARTICIPANT),
                new GroupMemberData("친구 모임2", "이덕혜", ClubMemberRole.PARTICIPANT),
                new GroupMemberData("독서 모임", "레베카", ClubMemberRole.PARTICIPANT),
                new GroupMemberData("친구 모임2", "김암호", ClubMemberRole.PARTICIPANT) //암호화 테스트용 데이터
        );

        for (GroupMemberData gm : groupMembers) {
            Club club = clubs.get(gm.clubName());
            Member member = members.get(gm.memberNickname());

            ClubMember clubMember = ClubMember.builder()
                    .member(member)
                    .club(club)
                    .role(gm.role())
                    .state(ClubMemberState.JOINING)
                    .build();

            clubMemberRepository.save(clubMember);
        }
    }


    /**
     * 모임 일정 초기 데이터 설정
     */
    @Transactional
    public void initScheduleTestData() {
        // 모임 1의 일정 초기 데이터
        Club club1 = clubs.get("산책 모임");

        for (int i = 1; i <= 4; i++) {
            Schedule schedule = Schedule.builder()
                    .title("제 %s회 걷기 일정".formatted(i))
                    .content("서울에서 함께 산책합니다")
                    .startDate(LocalDateTime.parse("2025-07-05T10:00:00").plusDays(i * 7))
                    .endDate(LocalDateTime.parse("2025-07-05T15:00:00").plusDays(i * 7))
                    .spot("서울시 서초동")
                    .club(club1)
                    .build();
            scheduleRepository.save(schedule);
        }

        // 모임 2의 일정 초기 데이터
        Club club2 = clubs.get("친구 모임");

        Schedule schedule2 = Schedule.builder()
                .title("맛집 탐방")
                .content("시장 맛집 탐방")
                .startDate(LocalDateTime.parse("2025-05-07T18:00:00"))
                .endDate(LocalDateTime.parse("2025-05-07T21:30:00"))
                .spot("단양시장")
                .club(club2)
                .build();
        scheduleRepository.save(schedule2);

        Schedule schedule3 = Schedule.builder()
                .title("강릉 여행")
                .content("1박 2일 강릉 여행")
                .startDate(LocalDateTime.parse("2025-07-23T08:10:00"))
                .endDate(LocalDateTime.parse("2025-07-24T15:00:00"))
                .spot("강릉")
                .club(club2)
                .build();
        scheduleRepository.save(schedule3);

        // 모임 3의 일정 초기 데이터
        Club club3 = clubs.get("친구 모임2");
        Schedule schedule4 = Schedule.builder()
                .title("제주도 여행")
                .content("제주도에서 함께 여행해요")
                .startDate(LocalDateTime.parse("2025-07-01T09:00:00"))
                .endDate(LocalDateTime.parse("2025-07-05T18:00:00"))
                .spot("제주도")
                .club(club3)
                .build();
        scheduleRepository.save(schedule4);

        // 모임 3의 일정 초기 데이터 - 비활성화된 일정
        Schedule schedule5 = Schedule.builder()
                .title("제주도 여행 (비활성화)")
                .content("제주도에서 함께 여행해요")
                .startDate(LocalDateTime.parse("2025-10-01T09:00:00"))
                .endDate(LocalDateTime.parse("2025-10-05T18:00:00"))
                .spot("제주도")
                .club(club3)
                .build();
        scheduleRepository.save(schedule5);
        schedule5.deactivate();

        // 모임 4의 일정 초기 데이터
        Club club4 = clubs.get("A도시 러닝 대회");
        Schedule schedule6 = Schedule.builder()
                .title("A도시 러닝 대회")
                .content("A도시에서 열리는 러닝 대회에 참여해요")
                .startDate(LocalDateTime.parse("2025-08-10T07:00:00"))
                .endDate(LocalDateTime.parse("2025-08-10T12:00:00"))
                .spot("서울 A도시")
                .club(club4)
                .build();
        scheduleRepository.save(schedule6);

        // 종료된 모임 일정
        Club nClub1 = clubs.get("독서 모임");
        Schedule nSchedule1 = Schedule.builder()
                .title("독서 모임 일정")
                .content("부산에서 함께 독서해요")
                .startDate(LocalDateTime.parse("2025-07-12T10:00:00"))
                .endDate(LocalDateTime.parse("2025-07-12T15:00:00"))
                .spot("부산")
                .club(nClub1)
                .build();
        scheduleRepository.save(nSchedule1);
    }

    /**
     * 모임의 체크리스트 초기 데이터 설정
     */
    @Transactional
    public void initCheckListTestData() {
        List<String> clubNames = List.of("산책 모임", "친구 모임", "친구 모임2", "A도시 러닝 대회");

        for (String clubName : clubNames) {
            Club club = clubs.get(clubName);
            if (club == null) continue;

            List<Schedule> club1Schedules = scheduleRepository.findByClubIdOrderByStartDate(club.getId());

            for (Schedule schedule : club1Schedules) {
                if (schedule.getTitle().equals("강릉 여행")) continue; // 체크리스트 없는 일정(테스트용)

                CheckList checkList = CheckList.builder()
                        .isActive(true)
                        .build();
                checkList.setSchedule(schedule);
                checkListRepository.save(checkList);
            }
        }
    }

    /**
     * 체크리스트 항목 초기 데이터 설정
     */
    @Transactional
    public void initCheckListItemTestData() {
        List<CheckList> allCheckLists = checkListRepository.findAll();

        for (CheckList checkList : allCheckLists) {
            // 각 체크리스트에 3개의 체크리스트 항목 생성
            for (int i = 1; i <= 3; i++) {
                CheckListItem item = CheckListItem.builder()
                        .content("체크리스트 항목 " + i)
                        .isChecked(false)
                        .checkList(checkList)
                        .build();
                checkListItemRepository.save(item);
            }
        }
    }

    /**
     * 체크리스트 항목에 모임 맴버를 랜덤으로 할당
     */
    @Transactional
    public void initItemAssignTestData() {
        List<CheckListItem> allItems = checkListItemRepository.findAll();

        for (CheckListItem item : allItems) {
            Long clubId = item.getCheckList().getSchedule().getClub().getId();

            // 모임의 맴버들만 할당 대상
            List<ClubMember> clubMembers = clubMemberRepository.findAllByClubId(clubId);
            if (clubMembers.isEmpty()) {
                continue;
            }

            // 랜덤 할당
            int assignCount = 1 + (int) (Math.random());

            // 중복되지 않도록 할당
            Set<ClubMember> assignedMembers = new HashSet<>();
            for (int i = 0; i < assignCount; i++) {
                ClubMember assignee;

                // 중복되지 않는 멤버를 랜덤으로 선택
                do {
                    assignee = clubMembers.get((int) (Math.random() * clubMembers.size()));
                } while (assignedMembers.contains(assignee));
                assignedMembers.add(assignee);

                // 아이템 할당 생성
                ItemAssign assign = ItemAssign.builder()
                        .clubMember(assignee)
                        .checkListItem(item)
                        .build();

                assignee.addItemAssign(assign);
                itemAssignRepository.save(assign);
            }
        }
    }

    /**
     * 회원 생성 메서드
     */
    private Member createMember(String nickname, String password, String email, String bio) {
        Member member = Member.builder()
                .nickname(nickname)
                .password(password)
                .memberType(MemberType.MEMBER)
                .tag(UUID.randomUUID().toString().substring(0, 5))
                .build();
        memberRepository.save(member);

        if (email == null) return member;

        MemberInfo info = MemberInfo.builder()
                .email(email)
                .bio(bio)
                .member(member)
                .build();
        memberInfoRepository.save(info);

        member.setMemberInfo(info);
        return member;
    }

    /**
     * 회원 생성 메서드 2 - 비밀번호 암호화 테스트용
     */
    private Member createEncodedMember(String nickname, String password, String email, String bio) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        Member member = Member.createGuest(nickname, passwordEncoder.encode(password), "2344");
        memberRepository.save(member);

        if (email == null) return member;

        MemberInfo info = MemberInfo.builder()
                .email(email)
                .bio(bio)
                .member(member)
                .build();
        memberInfoRepository.save(info);

        member.setMemberInfo(info);
        return member;
    }
}