package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public interface ReservationJpaRepository extends
        ReservationRepository,
        ListCrudRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    List<BookStatus> NON_CANCELLED_STATUSES = List.of(
            BookStatus.WAITING, BookStatus.BOOKED
    );

    @Override
    boolean existsByTimeId(long timeId);

    @Override
    boolean existsByThemeId(long themeId);

    @Override
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    @Override
    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
    }

    @Override
    default Optional<Reservation> findFirstWaiting(Theme theme, LocalDate date, ReservationTime time) {
        return findFirstWaiting(theme, date, time, BookStatus.WAITING);
    }

    @Query("""
            select r from Reservation r
            where r.theme = :theme
            and r.date = :date
            and r.time = :time
            and r.status = :status
            order by r.createdAt asc
            limit 1
             """)
    Optional<Reservation> findFirstWaiting(
            Theme theme, LocalDate date, ReservationTime time, BookStatus status
    );

    @Override
    default long getWaitingCount(Reservation reservation) {
        return getWaitingCount(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                NON_CANCELLED_STATUSES,
                reservation.getCreatedAt()
        );
    }

    @Query("""
            select count(r) from Reservation r
            where r.theme = :theme
            and r.date = :date
            and r.time = :time
            and r.status in :statuses
            and r.createdAt < :createdAt
            """)
    long getWaitingCount(
            Theme theme, LocalDate date, ReservationTime time, List<BookStatus> statuses, LocalDateTime createdAt
    );

    @Override
    default List<Reservation> findActiveReservationByMemberId(long memberId) {
        return findAllByStatusInAndMemberId(NON_CANCELLED_STATUSES, memberId);
    }

    List<Reservation> findAllByStatusInAndMemberId(List<BookStatus> statuses, long memberId);

    @Override
    default boolean existsAlreadyWaitingOrBooked(long memberId, long themeId, LocalDate date, long timeId) {
        return existsAlreadyWaitingOrBooked(memberId, themeId, date, timeId, NON_CANCELLED_STATUSES);
    }

    @Query("""
            select count(r) > 0 from Reservation r
            where r.member.id = :memberId
            and r.theme.id = :themeId
            and r.date = :date
            and r.time.id = :timeId
            and r.status in :statuses
            """)
    boolean existsAlreadyWaitingOrBooked(
            long memberId, long themeId, LocalDate date, long timeId, List<BookStatus> statuses
    );

    @Override
    default List<Reservation> findAllBookedReservations() {
        return findAllByStatus(BookStatus.BOOKED);
    }

    @Override
    default List<Reservation> findAllWaitingReservations() {
        return findAllByStatus(BookStatus.WAITING);
    }

    List<Reservation> findAllByStatus(BookStatus status);
}
