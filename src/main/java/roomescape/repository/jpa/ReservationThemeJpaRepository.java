package roomescape.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;

@Repository
public interface ReservationThemeJpaRepository extends JpaRepository<ReservationTheme, Long> {

    boolean existsByName(final String name);
}
