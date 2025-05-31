package roomescape.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.NotFoundException;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.model.Member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member findByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

    @Override
    public Member findById(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }
}
