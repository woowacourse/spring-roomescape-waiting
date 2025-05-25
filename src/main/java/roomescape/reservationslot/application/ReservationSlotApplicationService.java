package roomescape.reservationslot.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.domain.service.ReservationSlotDomainService;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.service.ReservationTimeDomainService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.service.ThemeDomainService;
import roomescape.reservation.domain.service.ReservationDomainService;

@Service
public class ReservationSlotApplicationService {

    private final ReservationSlotDomainService reservationSlotDomainService;
    private final ReservationTimeDomainService reservationTimeDomainService;
    private final ThemeDomainService themeDomainService;
    private final MemberDomainService memberDomainService;
    private final ReservationDomainService reservationDomainService;

    public ReservationSlotApplicationService(final ReservationSlotDomainService reservationSlotDomainService,
                                             final ReservationTimeDomainService reservationTimeDomainService,
                                             final ThemeDomainService themeDomainService,
                                             final MemberDomainService memberDomainService,
                                             final ReservationDomainService slotReservationDomainService) {
        this.reservationSlotDomainService = reservationSlotDomainService;
        this.reservationTimeDomainService = reservationTimeDomainService;
        this.themeDomainService = themeDomainService;
        this.memberDomainService = memberDomainService;
        this.reservationDomainService = slotReservationDomainService;
    }

    public List<ReservationResponse> findReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        List<ReservationSlot> filteredReservations = reservationSlotDomainService.findFilteredReservations(themeId, memberId,
                startDate, endDate);
        return filteredReservations
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getTime();
                    Theme theme = reservation.getTheme();
                    Member member = reservation.findReservedMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    public void delete(Long id) {
        reservationSlotDomainService.delete(id);
    }

    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        reservationSlotDomainService.checkIfReservationDoesNotExists(date, timeId, themeId);
        ReservationTime time = reservationTimeDomainService.findReservationTime(timeId);
        Theme theme = themeDomainService.findTheme(themeId);
        Member member = memberDomainService.getMember(memberId);

        ReservationSlot newReservationSlot = reservationSlotDomainService.save(member, date, time, theme, now);
        return ReservationResponse.of(newReservationSlot, time, theme, member);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDomainService.findMyReservations(memberInfo);
    }
}
