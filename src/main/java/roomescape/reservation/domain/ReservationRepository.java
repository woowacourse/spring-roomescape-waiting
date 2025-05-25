package roomescape.reservation.domain;

import roomescape.reservation.application.dto.ReservationIdWithSequenceResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsByParams(ReservationId id);

    boolean existsBySlot(ReservationSlot slot);

    boolean existsBySlotAndUserId(ReservationSlot slot, UserId userId);

    Optional<Reservation> findById(ReservationId id);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(UserId userId);

    Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> findAllByParams(UserId userId, ThemeId themeId, ReservationDate reservationDate, ReservationDate reservationDate1);

    List<Reservation> findAllBySlot(ReservationSlot slot);

    List<Reservation> findAllBySlotAndCreatedAt(ReservationSlot slot, LocalDateTime createdAt);

    List<ReservationIdWithSequenceResponse> findAllReservationSequencesByIds(final List<ReservationId> ids);

    Reservation save(Reservation reservation);

    void deleteById(ReservationId id);

    void delete(Reservation target);
}
