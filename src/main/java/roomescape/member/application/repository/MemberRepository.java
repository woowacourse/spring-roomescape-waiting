package roomescape.member.application.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.SignUpRequest;

public interface MemberRepository {
    Member insert(SignUpRequest signUpRequest);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    List<Member> findAllMembers();
}
