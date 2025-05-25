package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.waiting.domain.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.member
            JOIN FETCH w.spec.theme
            JOIN FETCH w.spec.time
            """)
    List<Waiting> findAllWithEagerLoading();

    default List<Waiting> findBySpec(ReservationSpec spec) {
        return findBySpecDateValueAndSpecTimeIdAndSpecThemeId(spec.getDate().getValue(), spec.getTime().getId(),
                spec.getTheme().getId());
    }

    List<Waiting> findBySpecDateValueAndSpecTimeIdAndSpecThemeId(LocalDate date, Long timeId, Long themeId);

    List<Waiting> findByMemberId(Long memberId);
}
