package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import roomescape.domain.Reservation;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    @Query(value = """
            SELECT reservation.id, reservation.date,
                            `time`.id AS time_id, `time`.start_at AS time_start_at,
                            theme.id AS theme_id, theme.name AS theme_name,
                            theme.description AS theme_description, theme.thumbnail AS theme_thumbnail,
                            member.id AS member_id, member.name AS member_name, member.email AS member_email, member.role AS member_role
                            FROM reservation
                            INNER JOIN member ON reservation.member_id = member.id
                            INNER JOIN theme ON reservation.theme_id = theme.id
                            INNER JOIN reservation_time AS `time` ON reservation.time_id = `time`.id
                            WHERE date >= ? AND date <= ?
                            AND member.name = ?
                            AND theme.name = ?
            """, nativeQuery = true)
    List<Reservation> findByPeriodAndMemberAndTheme(LocalDate start, LocalDate end, String memberName,
                                                    String themeName);

    List<Reservation> findAll();

    @Query(value = "SELECT time_id FROM reservation WHERE date = ? AND theme_id = ?", nativeQuery = true)
    List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void delete(Reservation reservation);

    void deleteAll();
}
