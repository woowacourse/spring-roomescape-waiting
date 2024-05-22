package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.domain.repository.*;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.WaitingRequest;
import roomescape.service.dto.response.MemberResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.response.ThemeResponse;
import roomescape.service.dto.response.WaitingResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class WaitingService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository timeRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.waitingRepository = waitingRepository;
    }

    public WaitingResponse saveWaiting(WaitingRequest waitingRequest, long memberId) {
        Reservation alreadyBookedReservation = findReservation(waitingRequest);
        Waiting waiting = createWaiting(waitingRequest, memberId);

        validateReservationMember(waiting, alreadyBookedReservation);
        validateDuplicatedWaiting(waiting);

        alreadyBookedReservation.addWaiting(waiting);
        Waiting savedWaiting = waitingRepository.save(waiting);

        return createWaitingResponse(savedWaiting);
    }

    public void deleteWaiting(long id) {
        Waiting waiting = findWaitingById(id);
        waiting.delete();
        waitingRepository.delete(waiting);
    }

    private void validateReservationMember(Waiting waiting, Reservation alreadyBookedReservation){
        Member requestMember = waiting.getMember();
        Member alreadyBookedMember = alreadyBookedReservation.getMember();
        System.out.println(requestMember.equals(alreadyBookedMember));
        if(requestMember.equals(alreadyBookedMember)){
            throw new RoomEscapeBusinessException("예약에 성공한 유저는 대기를 요청할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(Waiting waiting) {
        if(waiting.getMember().hasWaiting(waiting)){
            throw new RoomEscapeBusinessException("중복 예약 대기는 불가합니다.");
        }
    }

    private WaitingResponse createWaitingResponse(Waiting savedWaiting) {
        return new WaitingResponse(
                savedWaiting.getId(),
                MemberResponse.from(savedWaiting.getMember()),
                savedWaiting.getReservation().getDate(),
                new ReservationTimeResponse(savedWaiting.getReservation().getTime()),
                new ThemeResponse(savedWaiting.getReservation().getTheme())
        );
    }

    private Waiting createWaiting(WaitingRequest waitingRequest, long memberId) {
        return new Waiting(
                LocalDateTime.now(),
                findMemberById(memberId),
                findReservation(waitingRequest)
        );
    }

    private Waiting findWaitingById(long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("예약 대기 기록을 찾을 수 없습니다."));
    }

    private Reservation findReservation(WaitingRequest waitingRequest) {
        return reservationRepository.findByDateAndThemeAndTime(
                waitingRequest.date(),
                findThemeById(waitingRequest.themeId()),
                findTimeById(waitingRequest.timeId())
        ).orElseThrow(() -> new RoomEscapeBusinessException("예약이 없습니다"));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }
}
