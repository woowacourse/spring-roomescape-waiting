package roomescape.reservation.application.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.presentation.dto.WaitingRequest;
import roomescape.reservation.presentation.dto.WaitingResponse;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final WaitingRepository waitingRepository,
                          final ReservationRepository reservationRepository,
                          final ReservationTimeRepository reservationTimeRepository,
                          final ThemeRepository themeRepository, final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WaitingResponse createWaiting(final WaitingRequest waitingRequest, final Long memberId) {
        Member member = findMemberById(memberId);

        ReservationTime reservationTime = getReservationTime(waitingRequest.getTimeId());
        Theme theme = getTheme(waitingRequest.getThemeId());
        LocalDate date = waitingRequest.getDate();
        validateReservationDateTime(date, reservationTime);

        final Waiting waiting = new Waiting(
                member,
                theme,
                date,
                reservationTime
        );

        return new WaitingResponse(waitingRepository.save(waiting));
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> getWaitings(final Long memberId) {
        findMemberById(memberId);

        return waitingRepository.findByMemberId(memberId).stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> getWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional
    public void deleteWaiting(final Long id) {
        final Waiting waiting = findWaitingById(id);

        waitingRepository.delete(waiting);
    }

    @Transactional
    public void acceptWaiting(final Long id) {
        final Waiting waiting = findWaitingById(id);

        final Reservation reservation = new Reservation(
                waiting.getMember(),
                waiting.getReservationInfo()
        );

        waitingRepository.delete(waiting);
        reservationRepository.save(reservation);
    }

    private Waiting findWaitingById(final Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간 정보를 찾을 수 없습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("테마 정보를 찾을 수 없습니다."));
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
    }

    private void validateReservationDateTime(LocalDate reservationDate, ReservationTime reservationTime) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime.getStartAt());

        validateIsPast(reservationDateTime);
    }

    private static void validateIsPast(LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new DateTimeException("지난 일시에 대한 예약 대기 생성은 불가능합니다.");
        }
    }
}
