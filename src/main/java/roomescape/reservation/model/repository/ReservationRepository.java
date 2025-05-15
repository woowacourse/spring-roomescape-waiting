package roomescape.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.dto.ReservationWithMember;

public interface ReservationRepository {

    List<ReservationWithMember> getAllWithMember();

    Reservation save(Reservation reservation);

    Optional<ReservationWithMember> findWithMemberById(Long id);

    ReservationWithMember getWithMemberById(Long id);

    void remove(Reservation reservation);

    boolean existDuplicatedDateTime(LocalDate date, Long timeId, Long themeId);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    List<ReservationWithMember> getSearchReservationsWithMember(Long themeId, Long memberId, LocalDate from, LocalDate to);

    Optional<Reservation> findById(Long id);

    Reservation getById(Long id);
}
