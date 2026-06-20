package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.exception.RoomEscapeException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.member
        JOIN FETCH r.slot s
        JOIN FETCH s.time t
        JOIN FETCH s.theme
        ORDER BY s.date DESC, t.startAt ASC
        """)
    List<Reservation> findAll();

    @Override
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.member
        JOIN FETCH r.slot s
        JOIN FETCH s.time
        JOIN FETCH s.theme
        WHERE r.id = :id
        """)
    Optional<Reservation> findById(@Param("id") Long id);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.member m
        JOIN FETCH r.slot s
        JOIN FETCH s.time t
        JOIN FETCH s.theme
        WHERE m.name = :name
        ORDER BY s.date DESC, t.startAt ASC
        """)
    List<Reservation> findByName(@Param("name") String name);

    @Query("""
        SELECT r.slot.time.id
        FROM Reservation r
        WHERE r.slot.date = :date AND r.slot.theme.id = :themeId
        """)
    Set<Long> findReservedTimeIdsByDateAndThemeId(
        @Param("date") LocalDate date,
        @Param("themeId") Long themeId
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.time.id = :timeId")
    boolean existsByTimeId(@Param("timeId") Long timeId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.theme.id = :themeId")
    boolean existsByThemeId(@Param("themeId") Long themeId);

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.slot.date = :date
          AND r.slot.time.id = :timeId
          AND r.slot.theme.id = :themeId
        """)
    boolean existsBySlot(
        @Param("date") LocalDate date,
        @Param("timeId") Long timeId,
        @Param("themeId") Long themeId
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.member.name = :name
          AND r.slot.date = :date
          AND r.slot.time.id = :timeId
          AND r.slot.theme.id = :themeId
        """)
    boolean existsByNameAndSlot(
        @Param("name") String name,
        @Param("date") LocalDate date,
        @Param("timeId") Long timeId,
        @Param("themeId") Long themeId
    );

    default boolean existsBy(Reservation reservation) {
        return existsBySlot(
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
    }

    default boolean existsBySameUser(Reservation reservation) {
        return existsByNameAndSlot(
            reservation.getMember().getName(),
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
    }

    default Reservation getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_NOT_FOUND, message));
    }
}
