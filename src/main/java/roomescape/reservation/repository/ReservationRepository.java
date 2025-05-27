package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;

public interface ReservationRepository {

    boolean existsByTimeId(Long timeId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId, Long memberId);

    List<Reservation> findByParams(Long memberId, Long themeId, ReservationDate from,
                                   ReservationDate to);

    List<Reservation> findByParams(ReservationDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);
}
