package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataNotFoundException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationSlotService {
        private final ReservationTimeRepository reservationTimeRepository;
        private final ThemeRepository themeRepository;
        private final ReservationRepository reservationRepository;

        public List<AvailableReservationTime> findAvailableSlots(LocalDate date, Long themeId) {
                final List<AvailableReservationTime> availableReservationTimes = new ArrayList<>();
                final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
                final Theme theme = themeRepository.findById(themeId)
                                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));

                for (ReservationTime reservationTime : reservationTimes) {
                        ReservationSlot slot = new ReservationSlot(date, reservationTime, theme);
                        availableReservationTimes.add(new AvailableReservationTime(
                                        reservationTime.getId(),
                                        reservationTime.getStartAt(),
                                        reservationRepository.existsConfirmedReservationBySlot(slot)));
                }

                return availableReservationTimes;
        }
}
