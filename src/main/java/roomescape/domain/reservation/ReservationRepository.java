package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ReservationRepository {

    Reservation create(Reservation reservation);

    Optional<Reservation> findById(long id);

    List<Reservation> findAll();

    void deleteById(long id);

    boolean existsByTimeId(long timeId);

    boolean existsBy(LocalDate date, long timeId, long themeId);

    List<Reservation> findByMemberAndThemeBetweenDates(Long memberId, Long themeId,
                                                       LocalDate startDate, LocalDate endDate);

    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
    }
}
