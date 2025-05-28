package roomescape.reservation.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.dto.request.ConfirmedReservationByCriteriaWebRequest;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservation.application.dto.request.ReservationCreateWebRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationWebResponse;
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

    public ConfirmedReservationWebResponse create(final ConfirmedReservationCreateRequest request) {
        reservationSlotDataService.validateReservationSlotDoesNotExists(request.date(), request.timeId(),
                request.themeId());

        ReservationSlot slot = createReservationSlot(
                new ReservationCreateWebRequest(request.date(), request.timeId(), request.themeId()));
        Member member = memberDataService.getById(request.memberId());
        slot.addReservation(member, request.now());
        ReservationSlot savedSlot = reservationSlotDataService.save(slot);

        return ConfirmedReservationWebResponse.of(savedSlot);
    }

    public List<ConfirmedReservationWebResponse> findByCriteria(
            final ConfirmedReservationByCriteriaWebRequest request) {
        List<Reservation> reservations = reservationDataService.findByCriteria(request.themeId(), request.memberId(),
                request.startDate(), request.endDate());
        return reservations
                .stream()
                .map(Reservation::getReservationSlot)
                .map(ConfirmedReservationWebResponse::of)
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(final Long memberId) {
        memberDataService.validateExists(memberId);
        return reservationDataService.findMyReservations(memberId);
    }

    public void cancel(final Long reservationId) {
        Reservation reservation = reservationDataService.getById(reservationId);
        reservationDataService.deleteById(reservationId);
        cleanupEmptyReservationSlot(reservation.getReservationSlot().getId());
    }

    private ReservationSlot createReservationSlot(final ReservationCreateWebRequest reservationCreateWebRequest) {
        ReservationTime time = reservationTimeDataService.getById(reservationCreateWebRequest.timeId());
        Theme theme = themeDataService.getById(reservationCreateWebRequest.themeId());
        return new ReservationSlot(reservationCreateWebRequest.date(), time, theme);
    }

    private void cleanupEmptyReservationSlot(final Long slotId) {
        if (reservationSlotDataService.hasSingleReservation(slotId)) {
            reservationSlotDataService.deleteById(slotId);
        }
    }
}
