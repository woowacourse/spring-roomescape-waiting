package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.repository.querydsl.ReservationRepositoryCustom;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
