package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Service
public class ReservationCompositeService {
    private final ReservationModuleService reservationModuleService;
    private final WaitingModuleService waitingModuleService;

    public ReservationCompositeService(final ReservationModuleService reservationModuleService,
                                       final WaitingModuleService waitingModuleService) {
        this.reservationModuleService = reservationModuleService;
        this.waitingModuleService = waitingModuleService;
    }

    public List<MyReservationResponse> findMyReservations(final UserInfo userInfo) {
        List<Reservation> myReservations = reservationModuleService.findMyReservations(userInfo);
        List<Waiting> myWaitings = waitingModuleService.findMyWaitings(userInfo);

        List<MyReservationResponse> myReservationResponses = new ArrayList<>();
        return Stream.concat(
                        myReservations.stream().map(MyReservationResponse::from),
                        myWaitings.stream().map(MyReservationResponse::from)
                )
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> findReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        return reservationModuleService.getReservations(themeId, memberId, startDate, endDate)
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getInfo().getTime();
                    Theme theme = reservation.getInfo().getTheme();
                    Member member = reservation.getMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        reservationModuleService.checkIfReservationExists(date, timeId, themeId);

        ReservationTime time = reservationModuleService.findReservationTime(timeId);
        Theme theme = reservationModuleService.findTheme(themeId);
        Member member = reservationModuleService.findUserByMemberId(memberId);

        Reservation newReservation = reservationModuleService.save(
                Reservation.createUpcomingReservationWithUnassignedId(
                        member,
                        new ReservationInfo(date, time, theme), now));
        return ReservationResponse.of(newReservation, time, theme, member);
    }

    public void delete(final Long id) {
    }
}
