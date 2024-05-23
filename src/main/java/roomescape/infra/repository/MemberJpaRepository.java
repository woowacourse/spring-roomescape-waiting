package roomescape.infra.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;

public interface MemberJpaRepository extends
        MemberRepository,
        Repository<Member, Long> {

    @Override
    Member save(Member user);

    @Override
    Optional<Member> findById(Long id);

    @Override
    Optional<Member> findByEmailAndPassword(String email, String password);

    @Override
    List<Member> findAll();

    @Override
    boolean existsByEmail(String email);

    @Override
    void delete(Member member);
}
