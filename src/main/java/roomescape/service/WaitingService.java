package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.waiting.WaitingResponse;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public WaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                          MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse createWaiting(ReservationCreate reservationInfo) {
        Reservation reservation = reservationRepository.findByDateAndThemeIdAndTimeId(
                reservationInfo.getDate(),
                reservationInfo.getThemeId(),
                reservationInfo.getTimeId()
        ).orElseThrow(ReservationNotFoundException::new);
        Member member = memberRepository.findByEmail(reservationInfo.getEmail())
                .orElseThrow(MemberNotFoundException::new);
        Waiting waiting = waitingRepository.save(new Waiting(reservation, member, LocalDateTime.now()));

        return new WaitingResponse(waiting);
    }

    public void deleteWaiting(String email, long reservationId) {
        waitingRepository.deleteByReservationIdAndMemberEmail(reservationId, email);
    }
}
