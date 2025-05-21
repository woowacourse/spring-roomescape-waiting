package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.domain.MemberRole;

@Repository
public class JpaMemberRepositoryAdapter implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    public JpaMemberRepositoryAdapter(final JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    @Override
    public List<Member> findByMemberRole(final MemberRole memberRole) {
        return jpaMemberRepository.findByMemberRole(memberRole);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return jpaMemberRepository.existsByEmail(email);
    }

    @Override
    public Member save(final Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return jpaMemberRepository.findById(id);
    }
}
