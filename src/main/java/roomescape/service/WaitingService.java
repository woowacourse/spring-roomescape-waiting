package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.entity.WaitingStatus;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.jpa.JpaWaitingRepository;

@Service
@Transactional
public class WaitingService {

    private final JpaWaitingRepository waitingRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public WaitingService(JpaWaitingRepository waitingRepository,
        JpaReservationRepository reservationRepository,
        JpaReservationTimeRepository reservationTimeRepository,
        JpaThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<WaitingResponse> findAllWaitings() {
        return waitingRepository.findAll().stream()
            .map(WaitingResponse::from)
            .toList();
    }

    public WaitingResponse addWaitingAfterNow(Member member, WaitingRequest request) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate date = request.date();

        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException("theme"));

        long rank = waitingRepository.countByDateAndThemeIdAndTimeId(date, theme.getId(),
            time.getId());

        validateDateTimeAfterNow(now, date, time);
        validateDuplicateReservationAboutMemberId(member, request);

        return WaitingResponse.from(
            waitingRepository.save(new Waiting(member, request.date(), time, theme, rank + 1)));
    }

    private void validateDuplicateReservationAboutMemberId(Member member, WaitingRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
            request.date(), request.timeId(), request.themeId(), member.getId())) {
            throw new DuplicatedException("reservation");
        }
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
            request.date(), request.timeId(), request.themeId(), member.getId())) {
            throw new DuplicatedException("waiting");
        }
    }

    private void validateDateTimeAfterNow(LocalDateTime now, LocalDate date, ReservationTime time) {
        if (date.isBefore(now.toLocalDate()) ||
            (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    public void updateWaitingAndReservationStatus(Long waitingId, WaitingStatus status) {
        Waiting waiting = waitingRepository.findById(waitingId)
            .orElseThrow(() -> new NotFoundException("waiting"));

        if (status == WaitingStatus.APPROVED) {
            approveWaiting(waiting);
        } else if (status == WaitingStatus.DENIED) {
            denyWaiting(waiting);
        } else {
            throw new InvalidInputException("Invalid status");
        }
    }

    private void approveWaiting(Waiting waiting) {
        validateDateTimeAfterNow(waiting.getDate(), waiting.getTime());
        validateDuplicateReservation(waiting);

        reservationRepository.save(new Reservation(
            waiting.getMember(),
            waiting.getDate(),
            waiting.getTime(),
            waiting.getTheme()));

        waitingRepository.deleteById(waiting.getId());
        updateWaitingRanksAfter(waiting);
    }

    private void denyWaiting(Waiting waiting) {
        waitingRepository.deleteById(waiting.getId());
        updateWaitingRanksAfter(waiting);
    }

    private void updateWaitingRanksAfter(Waiting waiting) {
        List<Waiting> remainings = waitingRepository.findByDateAndThemeIdAndTimeId(
            waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId());

        long baseRank = waiting.getRank();
        for (Waiting remaining : remainings) {
            if (remaining.getRank() > baseRank) {
                remaining.minusRank(1L);
            }
        }
    }

    private void validateDateTimeAfterNow(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        if (date.isBefore(now.toLocalDate()) ||
            (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 승인 불가능");
        }
    }

    private void validateDuplicateReservation(Waiting waiting) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
            waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new DuplicatedException("reservation");
        }
    }
}
