package roomescape.reservationslot.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.ReservationDataService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationResponse;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.domain.Theme;

@Service
public class ReservationSlotApplicationService {

    private final ReservationSlotDataService reservationSlotDataService;
    private final ReservationTimeDataService reservationTimeDataService;
    private final ThemeDataService themeDataService;
    private final MemberDataService memberDataService;
    private final ReservationDataService reservationDataService;

    public ReservationSlotApplicationService(final ReservationSlotDataService reservationSlotDataService,
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

    public ConfirmedReservationResponse createConfirmedReservation(final LocalDate date, final Long timeId,
                                                                   final Long themeId, final Long memberId,
                                                                   final LocalDateTime now) {
        reservationSlotDataService.validateReservationSlotDoesNotExists(date, timeId, themeId);

        ReservationTime time = reservationTimeDataService.getById(timeId);
        Theme theme = themeDataService.getById(themeId);
        Member member = memberDataService.getById(memberId);
        ReservationSlot reservationSlot = reservationSlotDataService.saveReservationSlotWithReservation(member, date,
                time, theme, now);

        Reservation reservation = reservationSlot.findConfirmedReservation();
        return ConfirmedReservationResponse.of(reservation, reservationSlot);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDataService.findMyReservations(memberInfo);
    }
}
