package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.service.ReservationDomainService;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.reservation.presentation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.service.ReservationTimeDomainService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.service.ThemeDomainService;

@Service
public class ReservationApplicationService {

    private final ReservationDomainService reservationDomainService;
    private final ReservationTimeDomainService reservationTimeDomainService;
    private final ThemeDomainService themeDomainService;
    private final MemberDomainService memberDomainService;

    public ReservationApplicationService(final ReservationDomainService reservationDomainService,
                                         final ReservationTimeDomainService reservationTimeDomainService,
                                         final ThemeDomainService themeDomainService,
                                         final MemberDomainService memberDomainService) {
        this.reservationDomainService = reservationDomainService;
        this.reservationTimeDomainService = reservationTimeDomainService;
        this.themeDomainService = themeDomainService;
        this.memberDomainService = memberDomainService;
    }

    public List<ReservationResponse> findReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        return reservationDomainService.getReservations(themeId, memberId, startDate, endDate)
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getTime();
                    Theme theme = reservation.getTheme();
                    Member member = reservation.getMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    public void delete(Long id) {
        reservationDomainService.delete(id);
    }

    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        reservationDomainService.checkIfReservationExists(date, timeId, themeId);
        ReservationTime time = reservationTimeDomainService.findReservationTime(timeId);
        Theme theme = themeDomainService.findTheme(themeId);
        Member member = memberDomainService.getMember(memberId);

        Reservation newReservation = reservationDomainService.save(member, date, time, theme, now);
        return ReservationResponse.of(newReservation, time, theme, member);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDomainService.findMyReservations(memberInfo);
    }
}
