package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.domain.Theme;

@Service
public class ConfirmedReservationApplicationService {

    private final ReservationSlotDataService reservationSlotDataService;
    private final ReservationTimeDataService reservationTimeDataService;
    private final ThemeDataService themeDataService;
    private final MemberDataService memberDataService;
    private final ReservationDataService reservationDataService;

    public ConfirmedReservationApplicationService(final ReservationSlotDataService reservationSlotDataService,
                                                  final ReservationTimeDataService reservationTimeDataService,
                                                  final ThemeDataService themeDataService,
                                                  final MemberDataService memberDataService,
                                                  final ReservationDataService slotReservationDataService) {
        this.reservationSlotDataService = reservationSlotDataService;
        this.reservationTimeDataService = reservationTimeDataService;
        this.themeDataService = themeDataService;
        this.memberDataService = memberDataService;
        this.reservationDataService = slotReservationDataService;
    }

    public ConfirmedReservationResponse create(final LocalDate date, final Long timeId,
                                               final Long themeId, final Long memberId,
                                               final LocalDateTime now) {
        reservationSlotDataService.validateReservationSlotDoesNotExists(date, timeId, themeId);

        ReservationSlot slot = createReservationSlot(date, timeId, themeId);
        Member member = memberDataService.getById(memberId);
        slot.addReservation(member, now);
        ReservationSlot savedSlot = reservationSlotDataService.save(slot);

        return ConfirmedReservationResponse.of(savedSlot);
    }

    public List<ConfirmedReservationResponse> findByCriteria(
            final Long themeId,
            final Long memberId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        List<Reservation> reservations = reservationDataService.findByCriteria(themeId, memberId, startDate, endDate);
        return reservations
                .stream()
                .map(Reservation::getReservationSlot)
                .map(ConfirmedReservationResponse::of)
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDataService.findMyReservations(memberInfo);
    }

    public void cancel(final Long reservationId) {
        Reservation reservation = reservationDataService.getById(reservationId);
        reservationDataService.deleteById(reservationId);
        cleanupEmptyReservationSlot(reservation.getReservationSlot().getId());
    }

    private ReservationSlot createReservationSlot(final LocalDate date, final Long timeId, final Long themeId) {
        ReservationTime time = reservationTimeDataService.getById(timeId);
        Theme theme = themeDataService.getById(themeId);
        return new ReservationSlot(date, time, theme);
    }

    private void cleanupEmptyReservationSlot(final Long slotId) {
        if (reservationSlotDataService.hasSingleReservation(slotId)) {
            reservationSlotDataService.deleteById(slotId);
        }
    }
}
