package roomescape.facade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.response.ReservationListResponse;
import roomescape.controller.dto.response.ReservationWaitListResponse;
import roomescape.controller.dto.response.ReservationWaitResponse;
import roomescape.controller.dto.response.WaitListResponse;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.exception.custom.AlreadyReservedException;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;

@Service
@Transactional(readOnly = true)
public class ReservationFacade {

    private final ReservationService reservationService;
    private final WaitService waitService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final Clock clock;

    public ReservationFacade(ReservationService reservationService, WaitService waitService,
                             ReservationTimeService reservationTimeService, ThemeService themeService,
                             MemberService memberService, Clock clock) {
        this.reservationService = reservationService;
        this.waitService = waitService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaitResponse save(ReservationCreateRequest request, Long memberId) {
        Member member = memberService.findMember(memberId);
        ReservationTime reservationTime = reservationTimeService.findReservationTime(request.timeId());
        Theme theme = themeService.findTheme(request.themeId());

        return saveReservationOrWait(request, member, reservationTime, theme);
    }

    public ReservationWaitListResponse findByMemberId(Long memberId) {
        ReservationListResponse reservationListResponse = ReservationListResponse.from(
                reservationService.findByMemberId(memberId));

        Waits waits = waitService.findByMemberId(memberId);
        Map<Wait, Long> waitWithOrder = waits.waitsWithOrder();
        WaitListResponse waitListResponse = WaitListResponse.from(waitWithOrder);

        return new ReservationWaitListResponse(reservationListResponse, waitListResponse);
    }

    public ReservationWaitListResponse findByName(String name) {
        ReservationListResponse reservationListResponse = ReservationListResponse.from(
                reservationService.findByName(name));

        Waits waits = waitService.findAll();
        Map<Wait, Long> waitWithOrderByName = waits.waitsWithOrderByName(name);
        WaitListResponse waitListResponse = WaitListResponse.from(waitWithOrderByName);

        return new ReservationWaitListResponse(reservationListResponse, waitListResponse);
    }

    public ReservationWaitListResponse findAll() {
        ReservationListResponse reservationListResponse = ReservationListResponse.from(
                reservationService.findAll());

        Waits waits = waitService.findAll();
        Map<Wait, Long> waitWithOrder = waits.waitsWithOrder();
        WaitListResponse waitListResponse = WaitListResponse.from(waitWithOrder);

        return new ReservationWaitListResponse(reservationListResponse, waitListResponse);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id);

        Slot slot = new Slot(reservation.getDate(), reservation.getTime(), reservation.getTheme());
        Waits waits = waitService.findBySlot(slot);

        if (waits.isEmptyWaitsBySlot(slot)) {
            reservationService.delete(reservation, false);
            return;
        }
        reservationService.deleteAndFlush(reservation, false);
        confirmFirstWait(waits.firstWaitBySlot(slot));
    }

    @Transactional
    public void deleteWait(Long id) {
        waitService.delete(id, false);
    }

    private ReservationWaitResponse saveReservationOrWait(ReservationCreateRequest request, Member member,
                                                          ReservationTime reservationTime, Theme theme) {
        Optional<Reservation> reservation = reservationService.findBySlot(request.date(), request.timeId(),
                request.themeId());
        if (reservation.isEmpty()) {
            return saveReservation(request, member, reservationTime, theme);
        }
        if (reservation.get().isSameUser(member)) {
            throw new AlreadyReservedException();
        }

        return saveWait(request, member, reservationTime, theme);
    }

    private ReservationWaitResponse saveReservation(ReservationCreateRequest request, Member member,
                                                    ReservationTime reservationTime, Theme theme) {
        Reservation newReservationWithoutId = request.toReservation(member, reservationTime, theme);
        Reservation newReservation = reservationService.save(newReservationWithoutId, false);
        return ReservationWaitResponse.from(newReservation);
    }

    private ReservationWaitResponse saveWait(ReservationCreateRequest request, Member member,
                                             ReservationTime reservationTime, Theme theme) {
        Wait newWaitWithoutId = request.toWait(LocalDateTime.now(clock), member, reservationTime, theme);
        Wait wait = waitService.save(newWaitWithoutId);
        return ReservationWaitResponse.from(wait, waitService.calculateOrder(wait));
    }

    private void confirmFirstWait(Wait firstWait) {
        Reservation reservationWithoutId = new Reservation(firstWait.getMember(),
                new Slot(firstWait.getReservationDate(), firstWait.getTime(), firstWait.getTheme()));
        reservationService.save(reservationWithoutId, true);
        waitService.delete(firstWait.getId(), true);
    }
}
