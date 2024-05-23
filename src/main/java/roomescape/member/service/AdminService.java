package roomescape.member.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.CreateReservationRequest;
import roomescape.member.dto.response.CreateReservationResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.ConfirmReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class AdminService {
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public AdminService(final MemberRepository memberRepository,
                        final ReservationTimeRepository reservationTimeRepository,
                        final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository,
                        WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public CreateReservationResponse createReservation(final CreateReservationRequest createReservationRequest) {
        Member member = getMember(createReservationRequest.memberId());
        Theme theme = getTheme(createReservationRequest.themeId());
        ReservationTime reservationTime = getReservationTime(createReservationRequest.timeId());

        Reservation reservation = reservationRepository.save(
                new Reservation(member, createReservationRequest.date(), reservationTime, theme));
        return CreateReservationResponse.from(reservation);
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    public ConfirmReservationResponse confirmWaiting(Long id) {
        Waiting waiting = findWaiting(id);
        Slot slot = waiting.getSlot();

        checkReservationExists(slot);
        checkFirstWaiting(slot, id);

        waitingRepository.deleteById(id);
        Reservation reservation = reservationRepository.save(new Reservation(waiting.getMember(), slot));

        return ConfirmReservationResponse.from(reservation);
    }

    private void checkReservationExists(Slot slot) {
        if (reservationRepository.existsBySlot(slot)) {
            throw new IllegalArgumentException("이미 예약이 존재하여 대기를 예약으로 변경할 수 없습니다.");
        }
    }

    private void checkFirstWaiting(Slot slot, Long id) {
        if (waitingRepository.existsBySlotAndIdLessThan(slot, id)) {
            throw new IllegalArgumentException(id + "번 예약 대기보다 앞선 대기가 존재하여 예약으로 변경할 수 없습니다.");
        }
    }

    private Waiting findWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 대기가 존재하지 않아 예약으로 변경할 수 없습니다."));
    }
}
