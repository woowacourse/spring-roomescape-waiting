package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void deleteById(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate from, LocalDate to);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAll();

    boolean existsByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);

    Optional<Reservation> findBy(LocalDate date, Long timeId, Long themeId);
}
