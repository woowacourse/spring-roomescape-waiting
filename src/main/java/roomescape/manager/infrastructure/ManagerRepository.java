package roomescape.manager.infrastructure;

import roomescape.manager.Manager;

import java.util.Optional;

public interface ManagerRepository {
    Optional<Manager> findByMemberId(long memberId);
}
