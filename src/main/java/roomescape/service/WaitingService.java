package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.CannotWaitingForMineException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservation.WaitingConflictException;
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

    public List<WaitingResponse> findAll() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::new)
                .toList();
    }

    public WaitingResponse createWaiting(ReservationCreate reservationInfo) {
        Reservation reservation = reservationRepository.findByDateAndThemeIdAndTimeId(
                reservationInfo.getDate(),
                reservationInfo.getThemeId(),
                reservationInfo.getTimeId()
        ).orElseThrow(ReservationNotFoundException::new);
        Member member = memberRepository.findByEmail(reservationInfo.getEmail())
                .orElseThrow(MemberNotFoundException::new);
        validateMineReservation(reservation, member);
        validateDuplicatedWaiting(reservation, member);

        Waiting waiting = waitingRepository.save(new Waiting(reservation, member, LocalDateTime.now()));

        return new WaitingResponse(waiting);
    }

    private void validateMineReservation(Reservation reservation, Member member) {
        if (reservation.getMember().getId().equals(member.getId())) {
            throw new CannotWaitingForMineException();
        }
    }

    private void validateDuplicatedWaiting(Reservation reservation, Member member) {
        if (waitingRepository.existsByReservationIdAndMemberEmail(reservation.getId(), member.getEmail())) {
            throw new WaitingConflictException();
        }
    }

    public void deleteWaiting(String email, long reservationId) {
        waitingRepository.deleteByReservationIdAndMemberEmail(reservationId, email);
    }
}
