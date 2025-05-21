package roomescape.reservation.application.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.application.repository.WaitingRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.presentation.dto.WaitingRequest;
import roomescape.reservation.presentation.dto.WaitingResponse;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final ReservationRepository reservationRepository, final WaitingRepository waitingRepository,
                          final ReservationTimeRepository reservationTimeRepository,
                          final ThemeRepository themeRepository, final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
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

    public List<WaitingResponse> getWaitings(final Long memberId) {
        findMemberById(memberId);

        return waitingRepository.findByMemberId(memberId).stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional
    public void deleteWaiting(final Long id) {
        final Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));

        waitingRepository.delete(waiting);
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간 정보를 찾을 수 없습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("테마 정보를 찾을 수 없습니다."));
    }

    private void validateReservationDateTime(LocalDate reservationDate, ReservationTime reservationTime) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime.getStartAt());

        validateIsPast(reservationDateTime);
        validateIsDuplicate(reservationDate, reservationTime);
    }

    private static void validateIsPast(LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new DateTimeException("지난 일시에 대한 예약 생성은 불가능합니다.");
        }
    }

    private void validateIsDuplicate(final LocalDate reservationDate, final ReservationTime reservationTime) {
        if (reservationRepository.existsByDateAndReservationTimeStartAt(reservationDate,
                reservationTime.getStartAt())) {
            throw new IllegalStateException("중복된 일시의 예약은 불가능합니다.");
        }
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
    }
}
