package roomescape.reservation.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.WaitingReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class WaitingReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse save(WaitingReservationSaveRequest saveRequest) {
        Reservation reservation = createWaitingReservation(saveRequest);
        validateMemberReservationUnique(reservation);
        validateWaitingAvailable(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    private Reservation createWaitingReservation(WaitingReservationSaveRequest saveRequest) {
        Member member = memberRepository.findById(saveRequest.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Theme theme = themeRepository.findById(saveRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        ReservationTime reservationTime = reservationTimeRepository.findById(saveRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        return saveRequest.toWaitingReservation(member, theme, reservationTime);
    }

    private void validateMemberReservationUnique(Reservation reservation) {
        Optional<Reservation> duplicatedReservation = reservationRepository.findFirstByDateAndReservationTimeAndThemeAndMember(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );
        duplicatedReservation.ifPresent(this::throwExceptionByStatus);
    }

    private void throwExceptionByStatus(Reservation memberReservation) {
        if (memberReservation.isWaitingReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약 대기한 내역이 있습니다.");
        }
        if (memberReservation.isSuccessReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약한 내역이 있습니다.");
        }
    }

    private void validateWaitingAvailable(Reservation reservation) {
        Optional<Reservation> savedReservation = reservationRepository.findFirstByDateAndReservationTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        if (savedReservation.isEmpty()) {
            throw new IllegalArgumentException("추가된 예약이 없습니다. 예약을 추가해 주세요.");
        }
    }

    public List<WaitingResponse> findAll() {
        return reservationRepository.findAllByStatus(Status.WAIT)
                .stream()
                .map(WaitingResponse::toResponse)
                .toList();
    }

    @Transactional
    public void approveReservation(Long id) {
        Reservation waitingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 대기 내역이 없습니다."));
        validateUniqueReservation(waitingReservation);
        waitingReservation.updateSuccessStatus();
    }

    private void validateUniqueReservation(Reservation reservation) {
        Optional<Reservation> savedReservation = reservationRepository.findFirstByDateAndReservationTimeAndThemeAndStatus(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                Status.SUCCESS
        );
        if (savedReservation.isPresent()) {
            throw new IllegalArgumentException("이미 확정된 예약이 있습니다.");
        }
    }
}
