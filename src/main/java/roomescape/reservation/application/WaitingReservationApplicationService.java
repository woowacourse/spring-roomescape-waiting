package roomescape.reservation.application;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.WaitingWebResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;

@Service
public class WaitingReservationApplicationService {

    private final ReservationSlotDataService reservationSlotDataService;
    private final MemberDataService memberDataService;
    private final ReservationDataService reservationDataService;

    public WaitingReservationApplicationService(final ReservationSlotDataService reservationSlotDataService,
                                                final MemberDataService memberDataService,
                                                final ReservationDataService reservationDataService) {
        this.reservationSlotDataService = reservationSlotDataService;
        this.memberDataService = memberDataService;
        this.reservationDataService = reservationDataService;
    }

    public ReservationResponse create(final WaitingReservationCreateRequest createRequest) {
        ReservationSlot slot = reservationSlotDataService.getReservationSlotByDateAndTimeAndTheme(createRequest.date(),
                createRequest.timeId(), createRequest.themeId());
        Member member = memberDataService.getById(createRequest.memberId());
        Reservation reservation = slot.addReservation(member, LocalDateTime.now());
        reservationDataService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    public List<WaitingWebResponse> findAll() {
        List<Reservation> reservations = reservationDataService.findAllWaitingReservations();
        return reservations.stream()
                .map(WaitingWebResponse::from)
                .toList();
    }

    public void cancelByReservationSlotIdAndMemberId(final Long reservationSlotId, final Long memberId) {
        Reservation reservation = reservationDataService.getByReservationSlotIdAndMemberId(reservationSlotId, memberId);
        reservationDataService.deleteByReservationSlotIdAndMemberId(reservationSlotId, memberId);
        ReservationSlot reservationSlot = reservationSlotDataService.getById(reservationSlotId);
        reservationSlot.getReservations().remove(reservation);
    }

    public void cancel(final Long reservationId) {
        Reservation reservation = reservationDataService.getById(reservationId);
        reservationDataService.cancel(reservation);
        ReservationSlot reservationSlot = reservationSlotDataService.getById(reservation.getReservationSlot().getId());
        reservationSlot.getReservations().remove(reservation);
    }
}
