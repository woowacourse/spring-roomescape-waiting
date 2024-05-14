package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    @Query("""
            select r from Reservation as r
            where r.member.id = :memberId and r.theme.id = :themeId
            and r.date between :startDate and :endDate
            """)
    List<Reservation> findByMemberAndThemeBetweenDates(Long memberId, Long themeId,
                                                       LocalDate startDate, LocalDate endDate);

    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
    }
}
