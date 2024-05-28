package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationWaitingResponse save(ReservationRequest request) {
        Member member = getMemberById(request.memberId());
        Reservation reservation = getReservationByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId());
        validateWaitingNotExists(member, reservation);
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(member, reservation));
        List<ReservationWaiting> reservations = reservationWaitingRepository.findAllByReservation(reservation);
        int rank = reservations.indexOf(waiting) + 1;
        return ReservationWaitingResponse.of(waiting, rank);
    }

    public boolean existsByMemberAndReservation(Member member, Reservation reservation) {
        return reservationWaitingRepository.existsByMemberAndReservation(member, reservation);
    }

    public void deleteReservationWaiting(Long id) {
        reservationWaitingRepository.deleteById(id);
    }

    private Member getMemberById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    private Reservation getReservationByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "%s, timeId=%d, themeId=%d에 해당하는 예약이 존재하지 않습니다.".formatted(date, timeId, themeId)
                ));
    }

    private void validateWaitingNotExists(Member member, Reservation reservation) {
        if (existsByMemberAndReservation(member, reservation)) {
            throw new ResourceNotFoundException("중복된 예약 대기를 신청할 수 없습니다.");
        }
    }
}
