package roomescape.member.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class AdminService {
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final AdminServiceValidator adminServiceValidator;

    public AdminService(MemberRepository memberRepository,
                        ReservationTimeRepository reservationTimeRepository,
                        ThemeRepository themeRepository,
                        ReservationRepository reservationRepository,
                        WaitingRepository waitingRepository, AdminServiceValidator adminServiceValidator) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.adminServiceValidator = adminServiceValidator;
    }

    public CreateReservationResponse createReservation(CreateReservationRequest createReservationRequest) {
        Member member = getMember(createReservationRequest.memberId());
        Theme theme = getTheme(createReservationRequest.themeId());
        ReservationTime reservationTime = getReservationTime(createReservationRequest.timeId());

        Reservation reservation = reservationRepository.save(
                new Reservation(member, createReservationRequest.date(), reservationTime, theme));
        return CreateReservationResponse.from(reservation);
    }

    public ConfirmReservationResponse confirmWaiting(Long id) {
        Waiting waiting = getWaiting(id);
        Slot slot = waiting.getSlot();

        adminServiceValidator.validateReservationExists(slot);
        adminServiceValidator.validateFirstWaiting(slot, id);

        waitingRepository.deleteById(id);
        Reservation reservation = reservationRepository.save(new Reservation(waiting.getMember(), slot));

        return ConfirmReservationResponse.from(reservation);
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

    private Waiting getWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 대기가 존재하지 않아 예약으로 변경할 수 없습니다."));
    }
}
