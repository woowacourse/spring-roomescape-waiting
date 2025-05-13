package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public Long save(final Member member, final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new DataExistException("해당 시간에 이미 예약된 테마입니다.");
        }

        final ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + timeId));
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));
        final Reservation reservation = new Reservation(member, date, reservationTime, theme);

        return reservationRepository.save(reservation);
    }

    public void deleteById(final Long id) {
        reservationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));

        reservationRepository.deleteById(id);
    }

    public Reservation getById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<AvailableReservationTime> findAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<AvailableReservationTime> availableReservationTimes = new ArrayList<>();
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        for (ReservationTime reservationTime : reservationTimes) {
            availableReservationTimes.add(new AvailableReservationTime(
                    reservationTime.getId(),
                    reservationTime.getStartAt(),
                    reservationRepository.existsByDateAndStartAtAndThemeId(
                            date,
                            reservationTime.getStartAt(),
                            themeId
                    ))
            );
        }

        return availableReservationTimes;
    }
}
