package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationInfo;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.WaitingResponse;

import java.time.LocalDateTime;
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
        ReservationInfo reservationInfo = reservationRepository.findReservationInfo(request.date(), request.timeId(), request.themeId(), ReservationStatus.RESERVED);
        validateWaitingTime(reservationInfo);
        Member member = memberService.findMemberByEmail(loginMember.email());
        validateExistsReservationByMember(reservationInfo, member);

        long rank = waitingRepository.countByReservationInfo(reservationInfo) + 1;
        Waiting waiting = Waiting.create(reservationInfo, member, rank);
        validateExistsWaitingByMember(waiting, member);

        Waiting saved = waitingRepository.save(waiting);
        return WaitingResponse.from(saved);
    }

    private void validateWaitingTime(ReservationInfo reservationInfo) {
        LocalDateTime waitingDateTime = LocalDateTime.of(reservationInfo.getDate(), reservationInfo.getTime().getStartAt());
        if (!waitingDateTime.isAfter(currentTimeService.now())) {
            throw new IllegalArgumentException("[ERROR] 현재 시간 이후로 예약 대기할 수 있습니다.");
        }
    }

    private void validateExistsReservationByMember(ReservationInfo reservationInfo, Member member) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndMember(reservationInfo.getDate(), reservationInfo.getTime(), reservationInfo.getTheme(), member)) {
            throw new IllegalArgumentException("[ERROR] 이미 해당 날짜, 해당 테마, 해당 시간에 예약이 존재합니다.");
        }
    }

    private void validateExistsWaitingByMember(Waiting waiting, Member member) {
        if (waitingRepository.existsByReservationInfoAndMember(waiting.getReservationInfo(), member)) {
            throw new IllegalArgumentException("[ERROR] 이미 해당 날짜, 해당 테마, 해당 시간에 예약 대기 중입니다.");
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

    public List<WaitingResponse> getWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return WaitingResponse.from(waitings);
    }

    public boolean existsWaitings(ReservationInfo reservationInfo) {
        return waitingRepository.existsByReservationInfo(reservationInfo);
    }

    public Waiting findFirstRankWaitingByReservationInfo(ReservationInfo reservationInfo) {
        return waitingRepository.findAllByReservationInfo(reservationInfo)
                .getFirst();
    }

    @Transactional
    public void updateWaitings(ReservationInfo reservationInfo, ReservationInfo newReservationInfo) {
        List<Waiting> waitings = waitingRepository.findAllByReservationInfo(reservationInfo);
        waitings.forEach(waiting ->
                waiting.updateRankAndReservationInfo(newReservationInfo));
    }
}
