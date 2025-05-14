package roomescape.reservation.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    boolean existsByParams(Long timeId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByParams(Long memberId, Long themeId, ReservationDate from, ReservationDate to);

    List<Long> findTimeIdByParams(ReservationDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(ReservationDate startDate, ReservationDate endDate, int count);
}
