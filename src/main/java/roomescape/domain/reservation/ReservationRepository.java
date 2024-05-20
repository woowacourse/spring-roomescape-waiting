package roomescape.domain.reservation;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    List<Reservation> findAllByMemberId(Long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    @Query("""
            select r from Reservation as r
            where (:memberId is null or r.member.id = :memberId)
            and (:themeId is null or r.theme.id = :themeId)
            and (:startDate is null or r.date >= :startDate)
            and (:endDate is null or r.date <= :endDate)
            """)
    List<Reservation> findByMemberAndThemeBetweenDates(@Nullable Long memberId, @Nullable Long themeId,
                                                       @Nullable LocalDate startDate, @Nullable LocalDate endDate);

    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
    }
}
