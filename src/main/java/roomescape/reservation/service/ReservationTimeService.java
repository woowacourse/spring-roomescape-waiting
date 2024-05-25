package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationJpaRepository;
import roomescape.reservation.repository.ReservationTimeJpaRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeJpaRepository;

import java.time.LocalDate;
import java.util.List;

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

    public ReservationTimeResponse getTime(Long id) {
        ReservationTime reservationTime = reservationTimeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간 id입니다. time_id = " + id));

        return new ReservationTimeResponse(reservationTime);
    }

    public List<ReservationTimeResponse> findTimes() {
        return reservationTimeJpaRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<ReservationTimeResponse> findTimesWithAlreadyBooked(LocalDate date, Long themeId) {
        Theme theme = themeJpaRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("id에 맞는 테마가 없습니다. themeId = " + themeId));

        List<Long> alreadyBookedTimeIds = reservationJpaRepository.findByDateAndTheme(date, theme)
                .stream()
                .map(reservation -> reservation.getReservationTime().getId())
                .toList();

        return reservationTimeJpaRepository.findAll()
                .stream()
                .map(reservationTime ->
                        new ReservationTimeResponse(reservationTime, reservationTime.isBelongTo(alreadyBookedTimeIds)))
                .toList();
    }

    public void deleteTime(Long id) {
        reservationTimeJpaRepository.deleteById(id);
    }
}
