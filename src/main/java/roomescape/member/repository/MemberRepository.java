package roomescape.member.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Query("""
    SELECT m
    FROM Member m
    WHERE m.role = :role
    """)
    List<Member> findAllByRole(MemberRole role);
}
