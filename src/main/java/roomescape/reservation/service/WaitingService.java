package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.CannotWaitWithoutReservationException;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedWaitingException;
import roomescape.exception.MemberNotFoundException;
import roomescape.exception.NotMyWaitingException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.dto.response.WaitingWithRankResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.infrastructure.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ThemeRepository themeRepository,
                          TimeSlotRepository timeSlotRepository, MemberRepository memberRepository,
                          ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.themeRepository = themeRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponse createWaiting(Long memberId, WaitingRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Theme theme = themeRepository.findById(request.themeId()).orElseThrow(ThemeNotFoundException::new);
        TimeSlot timeSlot = timeSlotRepository.findById(request.timeId()).orElseThrow(TimeSlotNotFoundException::new);
        validateAlreadyReserved(request.date(), member, theme, timeSlot);
        validateDuplicatedWaiting(request.date(), member, theme, timeSlot);
        validateReservationExist(request.date(), theme, timeSlot);

        Waiting waiting = Waiting.builder()
                .date(request.date())
                .member(member)
                .theme(theme)
                .timeSlot(timeSlot).build();
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateReservationExist(LocalDate date, Theme theme, TimeSlot timeSlot) {
        if (!reservationRepository.existsByDateAndThemeAndTimeSlot(date, theme, timeSlot)) {
            throw new CannotWaitWithoutReservationException();
        }
    }

    private void validateAlreadyReserved(LocalDate date, Member member, Theme theme, TimeSlot timeSlot) {
        if (reservationRepository.existsByDateAndMemberAndThemeAndTimeSlot(date, member, theme, timeSlot)) {
            throw new ExistedReservationException();
        }
    }

    private void validateDuplicatedWaiting(LocalDate date, Member member, Theme theme, TimeSlot timeSlot) {
        if (waitingRepository.existsByDateAndMemberAndThemeAndTimeSlot(date, member, theme, timeSlot)) {
            throw new ExistedWaitingException();
        }
    }

    public List<WaitingWithRankResponse> findWaitingByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        return waitingRepository.findByMemberIdWithRank(member.getId());
    }

    public void deleteWaitingById(Long memberId, Long waitingId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(WaitingNotFoundException::new);
        if (!waiting.isSameMember(member)) {
            throw new NotMyWaitingException();
        }
        waitingRepository.delete(waiting);
    }

    public void deleteWaitingById(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(WaitingNotFoundException::new);
        waitingRepository.delete(waiting);
    }

    public List<WaitingResponse> findAllWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void convertWaitingToReservation(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(WaitingNotFoundException::new);
        reservationRepository.findByDateAndTimeSlotAndTheme(
                waiting.getDate(), waiting.getTimeSlot(), waiting.getTheme()
        ).ifPresent(reservationRepository::delete);
        Reservation reservation = Reservation.builder()
                .date(waiting.getDate())
                .theme(waiting.getTheme())
                .member(waiting.getMember())
                .timeSlot(waiting.getTimeSlot()).build();
        reservationRepository.save(reservation);
        waitingRepository.delete(waiting);
    }
}
