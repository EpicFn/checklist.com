package com.back.domain.preset.preset.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.preset.preset.entity.Preset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresetRepository extends JpaRepository<Preset, Long> {
  List<Preset> findByOwner(Member member);
}
