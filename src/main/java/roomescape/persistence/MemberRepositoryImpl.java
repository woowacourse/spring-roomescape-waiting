package roomescape.persistence;

import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    public MemberRepositoryImpl(final JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        return jpaMemberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return jpaMemberRepository.findById(id);
    }

    @Override
    public Member save(final Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }
}
