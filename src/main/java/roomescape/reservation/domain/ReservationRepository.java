package roomescape.reservation.domain;

import roomescape.reservation.application.dto.ReservationIdWithSequenceResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsById(ReservationId id);

    boolean existsBySlot(ReservationSlot slot);

    boolean existsBySlotAndUserId(ReservationSlot slot, UserId userId);

    Optional<Reservation> findById(ReservationId id);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(UserId userId);

    List<Theme> findTopNThemesToBookedCountByParamsOrderByBookedCountDesc(ReservationDate startDate,
                                                                          ReservationDate endDate,
                                                                          int N);

    List<Reservation> findAllByParams(UserId userId,
                                      ThemeId themeId,
                                      ReservationDate reservationDate,
                                      ReservationDate reservationDate1);

    List<ReservationIdWithSequenceResponse> findAllReservationSequencesByIds(final List<ReservationId> ids);

    Optional<Reservation> findNextBySlotAndCreatedAt(ReservationSlot slot, LocalDateTime createdAt);

    Reservation save(Reservation reservation);

    void deleteById(ReservationId id);

    void delete(Reservation target);
}
