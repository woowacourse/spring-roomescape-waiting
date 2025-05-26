package roomescape.member.repository.jpa;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
public class JpaMemberRepositoryComposite implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    @Override
    public Member save(Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaMemberRepository.findById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaMemberRepository.existsByEmail(email);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaMemberRepository.findByEmail(email);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }

    @Override
    public Optional<Member> findByName(String name) {
        return jpaMemberRepository.findByName(name);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return jpaMemberRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaMemberRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaMemberRepository.existsByName(name);
    }
}
