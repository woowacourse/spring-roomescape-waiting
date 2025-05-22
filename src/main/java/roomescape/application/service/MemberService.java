package roomescape.application.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.dto.response.MemberResponseDto;
import roomescape.infrastructure.db.MemberJpaRepository;

@Service
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;

    public MemberService(final MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    public List<MemberResponseDto> findAll() {
        return memberJpaRepository.findAll().stream()
                .map(MemberResponseDto::new)
                .collect(Collectors.toList());
    }
}
