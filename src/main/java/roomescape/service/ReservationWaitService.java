package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWait;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationScheduleRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.service.request.CreateReservationWaitRequest;
import roomescape.service.response.MyReservationWaitResponse;
import roomescape.service.response.ReservationResponse;
import roomescape.service.response.ReservationWaitResponse;

@Service
public class ReservationWaitService {

    private final MemberRepository memberRepository;
    private final ReservationScheduleRepository reservationScheduleRepository;
    private final ReservationWaitRepository reservationWaitRepository;
    private final ReservationRepository reservationRepository;

    public ReservationWaitService(
            final MemberRepository memberRepository,
            final ReservationScheduleRepository reservationScheduleRepository,
            final ReservationWaitRepository reservationWaitRepository,
            final ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationScheduleRepository = reservationScheduleRepository;
        this.reservationWaitRepository = reservationWaitRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationWaitResponse createReservationWait(final CreateReservationWaitRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 멤버입니다."));
        ReservationSchedule schedule = reservationScheduleRepository.findByReservationTime_IdAndTheme_IdAndReservationDate_Date(
                request.time(),
                request.theme(),
                request.date()
        ).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 일정입니다."));
        ReservationWait saved = reservationWaitRepository.save(new ReservationWait(null, member, schedule));
        return ReservationWaitResponse.from(saved);
    }

    public ReservationResponse approveReservationWait(final Long waitId) {
        ReservationWait reservationWait = reservationWaitRepository.findById(waitId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 대기입니다."));
        ReservationSchedule schedule = reservationWait.getSchedule();
        reservationRepository.findByScheduleId(schedule.getId())
                .orElseThrow(() -> new IllegalStateException("예약 대기를 승인하려면 해당 예약 일정에 예약이 없어야 합니다."));
        Reservation savedReservation = reservationRepository.save(new Reservation(
                null,
                reservationWait.getMember(),
                reservationWait.getSchedule()
        ));
        reservationWaitRepository.deleteById(reservationWait.getId());
        return ReservationResponse.from(savedReservation);
    }

    public void deleteReservationWait(final Long waitId) {
        reservationWaitRepository.deleteById(waitId);
    }

    public List<MyReservationWaitResponse> findAllMyWaitReservation(final Long memberId) {
        List<ReservationWait> reservationWaits = reservationWaitRepository.findAllByMember_id(memberId);
        return MyReservationWaitResponse.from(reservationWaits);
    }

    public List<ReservationWaitResponse> getAllWaitReservation() {
        List<ReservationWait> reservationWaits = reservationWaitRepository.findAll();
        return ReservationWaitResponse.from(reservationWaits);
    }
}
