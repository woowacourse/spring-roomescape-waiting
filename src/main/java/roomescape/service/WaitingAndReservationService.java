package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.entity.Member;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.jpa.JpaWaitingRepository;

@Service
@Transactional
public class WaitingAndReservationService {

    private final JpaWaitingRepository waitingRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public WaitingAndReservationService(JpaWaitingRepository waitingRepository,
        JpaReservationRepository reservationRepository,
        JpaReservationTimeRepository reservationTimeRepository,
        JpaThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResponse addWaitingAfterNow(Member member, WaitingRequest request) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate date = request.date();

        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException("theme"));

        validateDateTimeAfterNow(now, date, time);
        validateDuplicateReservationAboutMemberId(member, request);

        return WaitingResponse.from(
            waitingRepository.save(new Waiting(member, request.date(), time, theme, now)));
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
}
