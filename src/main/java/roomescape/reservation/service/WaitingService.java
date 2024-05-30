package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exceptions.NotFoundException;
import roomescape.exceptions.ValidationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.WaitingResponse;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.repository.ReservationJpaRepository;
import roomescape.reservation.repository.WaitingJpaRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WaitingService {

    private final ReservationJpaRepository reservationJpaRepository;
    private final WaitingJpaRepository waitingJpaRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingService(ReservationJpaRepository reservationJpaRepository, WaitingJpaRepository waitingJpaRepository,
                          ReservationTimeService reservationTimeService,
                          ThemeService themeService
    ) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.waitingJpaRepository = waitingJpaRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @Transactional
    public ReservationResponse addWaiting(WaitingRequest waitingRequest, Member member) {
        ReservationTimeResponse timeResponse = reservationTimeService.getTime(waitingRequest.time());
        ThemeResponse themeResponse = themeService.getTheme(waitingRequest.theme());

        Waiting waiting = new Waiting(waitingRequest.date(),
                timeResponse.toReservationTime(),
                themeResponse.toTheme(),
                member);

        validateAvailableWaiting(member, waiting);
        Waiting savedWaiting = waitingJpaRepository.save(waiting);
        return new ReservationResponse(savedWaiting);
    }

    private void validateAvailableWaiting(Member member, Waiting waiting) {
        validateIsReserved(member, waiting.getTheme(), waiting.getDate(), waiting.getReservationTime());
        validateIsBeforeNow(waiting);
        validateIsDuplicated(waiting);
    }

    private void validateIsReserved(Member member, Theme theme, LocalDate date, ReservationTime reservationTime) {
        if (reservationJpaRepository.existsByMemberAndDateAndReservationTimeAndTheme(member, date, reservationTime, theme)) {
            throw new ValidationException("이미 예약한 회원입니다.");
        }
    }

    private void validateIsBeforeNow(Waiting waiting) {
        if (waiting.isBeforeNow()) {
            throw new ValidationException("과거 시간은 예약할 수 없습니다.");
        }
    }

    private void validateIsDuplicated(Waiting waiting) {
        if (waitingJpaRepository.existsByDateAndReservationTimeAndThemeAndMember(
                waiting.getDate(),
                waiting.getReservationTime(),
                waiting.getTheme(),
                waiting.getMember())
        ) {
            throw new ValidationException("이미 예약 대기중 입니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<WaitingWithRank> findWaitingsByMember(Member loginMember) {
        return waitingJpaRepository.findWaitingsWithRankByMemberId(loginMember.getId());
    }

    @Transactional
    public void deleteById(Long id, Member member) {
        Waiting waiting = waitingJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id와 일치하는 대기를 찾을 수 없습니다."));
        if (waiting.getMember().equals(member)) {
            waitingJpaRepository.delete(waiting);
        }
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> findWaitings() {
        return waitingJpaRepository.findAll()
                .stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Waiting> findWaitingByDateAndReservationTimeAndTheme(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme
    ) {
        return waitingJpaRepository.findTopByDateAndReservationTimeAndThemeOrderByIdAsc(date, reservationTime, theme);
    }
}
