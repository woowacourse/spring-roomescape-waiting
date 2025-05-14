package roomescape.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationThemeV2;

@Repository
public interface ReservationThemeJpaRepository extends JpaRepository<ReservationThemeV2, Long> {

}
