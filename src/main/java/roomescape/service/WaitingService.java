package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationDto;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.util.List;

@Transactional
@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(
            final ReservationRepository reservationRepository,
            final MemberRepository memberRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationResponse createReservationWaiting(final ReservationDto reservationDto) {
        final Member member = memberRepository.findById(reservationDto.memberId())
                .orElseThrow(() -> new IllegalArgumentException(reservationDto.memberId() + "에 해당하는 사용자가 없습니다."));
        final ReservationTime time = reservationTimeRepository.findById(reservationDto.timeId())
                .orElseThrow(() -> new IllegalArgumentException(reservationDto.timeId() + "에 해당하는 예약 시간이 없습니다."));
        final Theme theme = themeRepository.findById(reservationDto.themeId())
                .orElseThrow(() -> new IllegalArgumentException(reservationDto.themeId() + "에 해당하는 테마가 없습니다."));

        final Reservation waiting = reservationDto.toModel(member, time, theme, ReservationStatus.WAITING);
        validateExistReservation(waiting);
        validateAlreadyReserved(waiting);
        validateDuplicatedWaiting(waiting);
        return ReservationResponse.from(reservationRepository.save(waiting));
    }

    private void validateExistReservation(final Reservation waiting) {
        final boolean exists = reservationRepository.existsByThemeAndDateAndTimeAndStatus(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.RESERVED);
        if (!exists) {
            throw new IllegalStateException("예약이 없는 건에는 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateAlreadyReserved(final Reservation waiting) {
        final boolean exists = reservationRepository.existsByThemeAndDateAndTimeAndStatusAndMember(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.RESERVED, waiting.getMember());
        if (exists) {
            throw new IllegalStateException("이미 예약한 건에는 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(final Reservation waiting) {
        final boolean exists = reservationRepository.existsByThemeAndDateAndTimeAndStatusAndMember(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.WAITING, waiting.getMember());
        if (exists) {
            throw new IllegalStateException("중복해서 예약 대기를 할 수 없습니다.");
        }
    }

    public List<ReservationResponse> findReservationWaitings() {
        final List<Reservation> waitings = reservationRepository.findByStatus(ReservationStatus.WAITING);
        return waitings.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void approveReservationWaiting(final Long waitingId) {
        final Reservation waiting = reservationRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 예약 대기가 없습니다."));
        validateIsApprovable(waiting);
        waiting.toReserved();
    }

    private void validateIsApprovable(final Reservation waiting) {
        final boolean exists = reservationRepository.existsByThemeAndDateAndTimeAndStatus(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.RESERVED);
        if (exists) {
            throw new IllegalStateException("이미 예약이 존재하여 승인이 불가능합니다.");
        }
    }

    public void rejectReservationWaiting(final Long id) {
        validateExistsWaiting(id);
        validateIsReservation(id);
        reservationRepository.deleteById(id);
    }

    private void validateExistsWaiting(final Long id) {
        final boolean exists = reservationRepository.existsById(id);
        if (!exists) {
            throw new IllegalArgumentException("해당 ID의 예약 대기가 없습니다.");
        }
    }

    private void validateIsReservation(final Long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 예약 대기가 없습니다."));
        if (reservation.isReserved()) {
            throw new IllegalArgumentException("확정된 예약 건은 거절할 수 없습니다.");
        }
    }
}
