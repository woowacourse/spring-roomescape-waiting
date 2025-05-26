package roomescape.reservationslot.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.application.MemberDataService;
import roomescape.reservation.application.ReservationDataService;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.MyReservationSlotResponse;
import roomescape.reservationslot.presentation.dto.response.ReservationSlotResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.theme.domain.Theme;
import roomescape.theme.application.ThemeDataService;

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

    public List<ReservationSlotResponse> findReservations(final Long themeId, final Long memberId,
                                                          final LocalDate startDate,
                                                          final LocalDate endDate) {
        List<ReservationSlot> filteredReservations = reservationSlotDataService.findFilteredReservations(themeId,
                memberId, startDate, endDate);
        return filteredReservations
                .stream()
                .map(reservationSlot -> {
                    ReservationTime time = reservationSlot.getTime();
                    Theme theme = reservationSlot.getTheme();
                    Member member = reservationSlot.findReservedMember();
                    return ReservationSlotResponse.of(reservationSlot, time, theme, member);
                })
                .toList();
    }

    public void delete(Long id) {
        reservationSlotDataService.delete(id);
    }

    public ReservationSlotResponse create(final LocalDate date, final Long timeId, final Long themeId,
                                          final Long memberId, final LocalDateTime now) {
        reservationSlotDataService.checkIfReservationDoesNotExists(date, timeId, themeId);
        ReservationTime time = reservationTimeDataService.findReservationTime(timeId);
        Theme theme = themeDataService.findTheme(themeId);
        Member member = memberDataService.getMember(memberId);

        ReservationSlot newReservationSlot = reservationSlotDataService.save(member, date, time, theme, now);
        return ReservationSlotResponse.of(newReservationSlot, time, theme, member);
    }

    public List<MyReservationSlotResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDataService.findMyReservations(memberInfo);
    }
}
