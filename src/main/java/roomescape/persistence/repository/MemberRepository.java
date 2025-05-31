package roomescape.persistence.repository;

import roomescape.model.Member;

public interface MemberRepository {

    Member findByEmail(String email);

    Member findById(Long id);

}
