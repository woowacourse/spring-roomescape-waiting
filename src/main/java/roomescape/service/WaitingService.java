package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.domain.repository.*;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dbservice.ReservationDbService;
import roomescape.service.dto.request.WaitingRequest;
import roomescape.service.dto.response.WaitingResponse;

import java.time.LocalDateTime;

@Service
@Transactional
public class WaitingService {
    private final ReservationDbService reservationDbService;
    private final WaitingRepository waitingRepository;

    public WaitingService(ReservationDbService reservationDbService, WaitingRepository waitingRepository) {
        this.reservationDbService = reservationDbService;
        this.waitingRepository = waitingRepository;
    }

    public WaitingResponse saveWaiting(WaitingRequest waitingRequest, long memberId) {
        Reservation alreadyBookedReservation = reservationDbService.findReservation(waitingRequest.date(), waitingRequest.themeId(), waitingRequest.timeId());
        Waiting waiting = createWaiting(alreadyBookedReservation);

        validateAlreadyReservedMember(waiting, alreadyBookedReservation);
        validateDuplicatedWaiting(waiting);

        alreadyBookedReservation.addWaiting(waiting);
        Waiting savedWaiting = waitingRepository.save(waiting);

        return new WaitingResponse(savedWaiting);
    }

    public void deleteWaiting(long id) {
        Waiting waiting = findWaitingById(id);
        waiting.delete();
        waitingRepository.delete(waiting);
    }

    private void validateAlreadyReservedMember(Waiting waiting, Reservation alreadyBookedReservation){
        Member requestMember = waiting.getMember();
        Member alreadyBookedMember = alreadyBookedReservation.getMember();

        if(requestMember.equals(alreadyBookedMember)){
            throw new RoomEscapeBusinessException("예약에 성공한 유저는 대기를 요청할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(Waiting waiting) {
        if(waiting.getMember().hasWaiting(waiting)){
            throw new RoomEscapeBusinessException("중복 예약 대기는 불가합니다.");
        }
    }

    private Waiting createWaiting(Reservation alreadyBookedReservation) {
        return new Waiting(
                LocalDateTime.now(),
                alreadyBookedReservation.getMember(),
                alreadyBookedReservation
        );
    }

    private Waiting findWaitingById(long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("예약 대기 기록을 찾을 수 없습니다."));
    }
}
