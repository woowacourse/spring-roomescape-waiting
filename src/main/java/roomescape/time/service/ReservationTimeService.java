package roomescape.time.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.DuplicateSaveException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.ReservationTimeAddRequest;
import roomescape.time.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<AvailableTimeResponse> findAllWithBookStatus(LocalDate date, Long themeId) {
        List<Long> foundReservationTimeIds = reservationRepository
                .findByDateDateAndTheme_Id(date, themeId)
                .stream()
                .map(Reservation::getTimeId)
                .toList();
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        List<AvailableTimeResponse> availableTimeResponses = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimes) {
            availableTimeResponses.add(new AvailableTimeResponse(
                    reservationTime,
                    foundReservationTimeIds.contains(reservationTime.getId())
            ));
        }
        return availableTimeResponses;
    }

    public ReservationTimeResponse saveReservationTime(ReservationTimeAddRequest reservationTimeAddRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeAddRequest.startAt())) {
            throw new DuplicateSaveException("이미 존재하는 예약시간은 추가할 수 없습니다.");
        }
        ReservationTime saved = reservationTimeRepository.save(reservationTimeAddRequest.toReservationTime());
        return new ReservationTimeResponse(saved);
    }

    public void removeReservationTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
