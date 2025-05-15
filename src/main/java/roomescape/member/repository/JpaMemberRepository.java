package roomescape.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

@Repository
public interface JpaMemberRepository extends MemberRepository, JpaRepository<Member, Long> {

    @Override
    boolean existsByEmail(MemberEmail email);
}
