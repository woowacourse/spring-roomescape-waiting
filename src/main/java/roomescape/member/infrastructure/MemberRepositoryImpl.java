package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    @Override
    public Member save(final Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public void deleteById(final Long id) {
        jpaMemberRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(final String email) {
        return jpaMemberRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findById(final Long id) {
        return jpaMemberRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }
}
