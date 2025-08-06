package com.back.domain.club.clubLink.repository;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.clubLink.entity.ClubLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubLinkRepository extends CrudRepository<ClubLink, Integer> {
    List<ClubLink> findAll();

    Optional<ClubLink> findByClubAndExpiresAtAfter(Club club, LocalDateTime attr0);

    Optional<ClubLink> findByInviteCode(String inviteCode);
}
