package roomescape.repository.member;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    public MemberRepositoryImpl(JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    @Override
    public long add(Member member) {
        return jpaMemberRepository.save(member).getId();
    }

    @Override
    public Optional<Member> findById(long id) {
        return jpaMemberRepository.findById(id);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return jpaMemberRepository.findByUsername(username);
    }

    @Override
    public boolean existByUsername(String username) {
        return jpaMemberRepository.existsByUsername(username);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }
}
