package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.WaitingResponse;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final CurrentTimeService currentTimeService;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          MemberService memberService,
                          CurrentTimeService currentTimeService
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberService = memberService;
        this.currentTimeService = currentTimeService;
    }

    @Transactional
    public WaitingResponse createWaiting(ReservationCreateRequest request, LoginMember loginMember) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        Member member = memberService.findMemberByEmail(loginMember.email());
        validateExistsReservation(reservation, member);

        long rank = waitingRepository.countByReservation(reservation) + 1;
        Waiting waiting = Waiting.create(reservation, member, rank);
        validateWaiting(waiting, member);

        Waiting saved = waitingRepository.save(waiting);
        return WaitingResponse.from(saved);
    }

    private void validateExistsReservation(Reservation reservation, Member member) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndMember(reservation.getDate(), reservation.getTime(), reservation.getTheme(), member)) {
            throw new IllegalArgumentException("[ERROR] 이미 해당 날짜, 해당 테마, 해당 시간에 예약이 존재합니다.");
        }
    }

    private void validateWaiting(Waiting waiting, Member member) {
        if (waitingRepository.existsByReservationAndMember(waiting.getReservation(), member)) {
            throw new IllegalArgumentException("[ERROR] 이미 해당 날짜, 해당 테마, 해당 시간에 예약 대기 중입니다.");
        }

        if (waiting.isPast(currentTimeService.now())) {
            throw new IllegalArgumentException("[ERROR] 현재 시간 이후로 예약 대기할 수 있습니다.");
        }
    }

    public List<Waiting> findWaitingsByMember(Member member) {
        return waitingRepository.findAllByMember(member);
    }

    @Transactional
    public void deleteWaitingById(Long id) {
        Waiting waiting = findWaitingById(id);
        waitingRepository.deleteById(waiting.getId());
    }

    public Waiting findWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약 대기 건이 존재하지 않습니다."));
    }
}
