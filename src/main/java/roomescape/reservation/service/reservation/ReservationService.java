package roomescape.reservation.service.reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.PastDateException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.reservation.ReservationRepositoryInterface;
import roomescape.reservation.repository.time.ReservationTimeRepositoryInterface;
import roomescape.reservation.repository.waiting.WaitingRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepositoryInterface reservationRepository;
    private final ReservationTimeRepositoryInterface reservationTimeRepository;
    private final ThemeRepositoryInterface themeRepository;
    private final WaitingRepositoryInterface waitingRepository;

    @Transactional
    public Reservation save(final Member member, final LocalDate date, final Long timeId, final Long themeId) {
        validatePastDate(date);

        final ReservationTime reservationTime = reservationTimeRepository.findById(timeId);
        final Theme theme = themeRepository.findById(themeId);

        validateExistReservation(date, reservationTime, theme);
        validateExistWaiting(date, reservationTime, theme);

        final Reservation reservation = new Reservation(member, date, reservationTime, theme);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteById(final Long id) {
        final Reservation reservation = reservationRepository.findById(id);
        final Theme theme = reservation.getTheme();
        final LocalDate date = reservation.getDate();
        final ReservationTime time = reservation.getTime();

        if (waitingRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            waitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(theme, date, time)
                    .ifPresent(waiting -> {
                        reservationRepository.save(new Reservation(
                                waiting.getMember(),
                                waiting.getDate(),
                                waiting.getTime(),
                                waiting.getTheme()
                        ));
                        waitingRepository.deleteById(waiting.getId());
                    });
        }

        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTime> findAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<AvailableReservationTime> availableReservationTimes = new ArrayList<>();
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme theme = themeRepository.findById(themeId);

        for (ReservationTime reservationTime : reservationTimes) {
            reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme);
            availableReservationTimes.add(new AvailableReservationTime(
                    reservationTime.getId(),
                    reservationTime.getStartAt(),
                    reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme))
            );
        }

        return availableReservationTimes;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByMember(final Member member) {
        return reservationRepository.findByMember(member);
    }

    private void validateExistReservation(final LocalDate date, final ReservationTime reservationTime,
                                          final Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme)) {
            throw new DataExistException("이미 예약이 존재해 예약을 할 수 없습니다.");
        }
    }

    private void validateExistWaiting(final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        if (waitingRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme)) {
            throw new DataExistException("예약 대기가 존재해 예약을 할 수 없습니다.");
        }
    }

    private void validatePastDate(final LocalDate date) {
        if (!date.isAfter(LocalDate.now())) {
            throw new PastDateException(date);
        }
    }
}
