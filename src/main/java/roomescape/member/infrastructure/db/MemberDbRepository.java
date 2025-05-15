package roomescape.member.infrastructure.db;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;

@Repository
@RequiredArgsConstructor
public class MemberDbRepository implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return memberJpaRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public List<Member> getAll() {
        return memberJpaRepository.findAll();
    }

    @Override
    public Member getById(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id로 찾을 수 있는 멤버가 없습니다."));
    }

    @Override
    public void save(Member member) {
        memberJpaRepository.save(member);
    }
}
