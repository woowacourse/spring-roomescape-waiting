package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.CannotWaitWithoutReservationException;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedWaitingException;
import roomescape.exception.MemberNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
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
        if (reservationRepository.existsByDateAndMemberAndThemeAndTimeSlot(request.date(), member, theme, timeSlot)) {
            throw new ExistedReservationException();
        }
        if (waitingRepository.existsByDateAndMemberAndThemeAndTimeSlot(request.date(), member, theme, timeSlot)) {
            throw new ExistedWaitingException();
        }
        if (!reservationRepository.existsByDateAndThemeAndTimeSlot(request.date(), theme, timeSlot)) {
            throw new CannotWaitWithoutReservationException();
        }

        Waiting waiting = Waiting.builder()
                .date(request.date())
                .member(member)
                .theme(theme)
                .timeSlot(timeSlot).build();

        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    public List<WaitingWithRankResponse> findWaitingByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        return waitingRepository.findByMemberIdWithRank(member.getId());

    }
}
