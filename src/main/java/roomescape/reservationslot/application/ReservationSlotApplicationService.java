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
import roomescape.reservation.presentation.dto.response.TotalReservationResponse;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.MyReservationSlotResponse;
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

    public void delete(Long id) {
        reservationSlotDataService.delete(id);
    }

    public TotalReservationResponse create(final LocalDate date, final Long timeId, final Long themeId,
                                           final Long memberId, final LocalDateTime now) {
        reservationSlotDataService.checkIfReservationDoesNotExists(date, timeId, themeId);
        ReservationTime time = reservationTimeDataService.findReservationTime(timeId);
        Theme theme = themeDataService.findTheme(themeId);
        Member member = memberDataService.getMember(memberId);
        ReservationSlot reservationSlot = reservationSlotDataService.save(member, date, time, theme, now);
        Reservation reservation = getReservation(reservationSlot);
        return TotalReservationResponse.of(reservation, reservationSlot, time, theme, member);
    }

    public List<MyReservationSlotResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDataService.findMyReservations(memberInfo);
    }

    private Reservation getReservation(final ReservationSlot reservationSlot) {
        return reservationDataService.findByReservationSlot(reservationSlot)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));
    }
}
