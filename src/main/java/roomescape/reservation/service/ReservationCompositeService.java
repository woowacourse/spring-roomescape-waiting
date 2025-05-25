package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberModuleService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeModuleService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationCompositeService {

    private final ReservationModuleService reservationModuleService;
    private final WaitingModuleService waitingModuleService;
    private final MemberModuleService memberModuleService;
    private final ThemeService themeService;
    private final ReservationTimeModuleService reservationTimeModuleService;

    public ReservationCompositeService(final ReservationModuleService reservationModuleService,
                                       final WaitingModuleService waitingModuleService, MemberModuleService memberModuleService,
                                       ThemeService themeService, ReservationTimeModuleService reservationTimeModuleService) {
        this.reservationModuleService = reservationModuleService;
        this.waitingModuleService = waitingModuleService;
        this.memberModuleService = memberModuleService;
        this.themeService = themeService;
        this.reservationTimeModuleService = reservationTimeModuleService;
    }

    public List<MyReservationResponse> findMyReservations(final UserInfo userInfo) {
        List<Reservation> myReservations = reservationModuleService.findMyReservations(userInfo);
        List<WaitingWithRank> waitingWithRanks = waitingModuleService.findMyWaitingsWithRank(userInfo);

        List<MyReservationResponse> myReservationResponses = new ArrayList<>();
        return Stream.concat(
                        myReservations.stream().map(
                                MyReservationResponse::from),
                        waitingWithRanks.stream().map(MyReservationResponse::from)
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        if (!reservationModuleService.isReservationExists(date, timeId, themeId)) {
            return createReservation(date, timeId, themeId, memberId, now);
        }
        return createWaiting(date, timeId, themeId, memberId, now);
    }

    public ReservationResponse createReservation(final LocalDate date, final Long timeId, final Long themeId,
                                                  final Long memberId,
                                                  final LocalDateTime now) {
        reservationModuleService.checkIfReservationExists(date, timeId, themeId);
        ReservationTime time = reservationTimeModuleService.findReservationTime(timeId);
        Theme theme = themeService.findTheme(themeId);
        Member member = memberModuleService.findUserByMemberId(memberId);
        Reservation newReservation = reservationModuleService.save(
                Reservation.createUpcomingReservationWithUnassignedId(
                        member,
                        new ReservationInfo(date, time, theme), now));
        return ReservationResponse.of(newReservation, time, theme, member);
    }

    public ReservationResponse createWaiting(final LocalDate date, final Long timeId, final Long themeId,
                                              final Long memberId, final LocalDateTime now) {
        ReservationTime time = reservationTimeModuleService.findReservationTime(timeId);
        Theme theme = themeService.findTheme(themeId);
        Member member = memberModuleService.findUserByMemberId(memberId);
        int turn = waitingModuleService.findMaxOrderByDateAndTimeAndTheme(date, timeId,
                themeId);
        Waiting newWaiting = waitingModuleService.save(
                Waiting.createUpcomingReservationWithUnassignedId(
                        member,
                        turn + 1,
                        new ReservationInfo(date, time, theme), now));
        return ReservationResponse.of(newWaiting, time, theme, member);
    }

    @Transactional
    public void deleteReservation(final Long reservationId) {
        Reservation reservation = reservationModuleService.findById(reservationId);
        reservationModuleService.delete(reservationId);
        promoteWaiting(reservation.getInfo());
    }

    private void promoteWaiting(final ReservationInfo info) {
        if (!waitingModuleService.isWaitingExists(info)) {
            return;
        }
        Waiting waiting = waitingModuleService.findFirstWaitingOfInfo(info);
        createReservation(
                waiting.getInfo().getDate(),
                waiting.getInfo().getTime().getId(),
                waiting.getInfo().getTheme().getId(),
                waiting.getMember().getId(),
                LocalDateTime.now()
        );
        waitingModuleService.delete(waiting.getId());
    }


}
