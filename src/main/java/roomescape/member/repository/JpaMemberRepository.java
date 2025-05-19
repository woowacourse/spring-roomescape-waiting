package roomescape.member.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

@Repository
public interface JpaMemberRepository extends MemberRepository, JpaRepository<Member, Long> {

    @Override
    List<Member> findAll();

    @Override
    Member save(Member member);

    @Override
    boolean existsByEmail(MemberEmail email);
}
