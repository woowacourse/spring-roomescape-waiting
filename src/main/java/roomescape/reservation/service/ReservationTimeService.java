package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationJpaRepository;
import roomescape.reservation.repository.ReservationTimeJpaRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeJpaRepository;

// TODO: 테스트 추가
@Service
public class ReservationTimeService {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ReservationJpaRepository reservationJpaRepository;
    private final ThemeJpaRepository themeJpaRepository;

    public ReservationTimeService(
            ReservationTimeJpaRepository reservationTimeJpaRepository,
            ReservationJpaRepository ReservationJpaRepository,
            ThemeJpaRepository themeJpaRepository
    ) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.reservationJpaRepository = ReservationJpaRepository;
        this.themeJpaRepository = themeJpaRepository;
    }

    public ReservationTimeResponse addTime(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeJpaRepository.existsByStartAt(reservationTimeRequest.startAt())) {
            throw new DuplicationException("이미 존재하는 시간입니다.");
        }
        ReservationTime reservationTime = reservationTimeJpaRepository.save(reservationTimeRequest.toReservationTime());
        return new ReservationTimeResponse(reservationTime);
    }

    public ReservationTimeResponse getReservationTimeById(Long id) {
        return new ReservationTimeResponse(reservationTimeJpaRepository.getById(id));
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        List<ReservationTimeResponse> reservationTimeResponses = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimeJpaRepository.findAll()) {
            reservationTimeResponses.add(new ReservationTimeResponse(reservationTime));
        }
        return reservationTimeResponses;
    }

    public List<ReservationTimeResponse> findTimesWithAlreadyBooked(LocalDate date, Long themeId) {
        Theme theme = themeJpaRepository.getById(themeId);
        List<Long> alreadyBookedTimeIds = reservationJpaRepository.findByDateAndTheme(date, theme)
                .stream()
                .map(reservation -> reservation.getReservationTime().getId())
                .toList();

        List<ReservationTimeResponse> reservationTimeResponses = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimeJpaRepository.findAll()) {
            ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(
                    reservationTime,
                    reservationTime.isBelongTo(alreadyBookedTimeIds)
            );
            reservationTimeResponses.add(reservationTimeResponse);
        }
        return reservationTimeResponses;
    }

    public void deleteTime(Long id) {
        reservationTimeJpaRepository.deleteById(id);
    }
}
