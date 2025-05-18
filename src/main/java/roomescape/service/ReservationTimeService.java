package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;

@Service
@Transactional
public class ReservationTimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationRepository reservationRepository;

    public ReservationTimeService(final JpaReservationTimeRepository reservationTimeRepository,
                                  final JpaReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponseDto createReservationTime(final ReservationTimeCreateRequestDto requestDto) {
        ReservationTime requestTime = requestDto.createWithoutId();
        try {
            ReservationTime savedTime = reservationTimeRepository.save(requestTime);
            return ReservationTimeResponseDto.from(savedTime);
        } catch (IllegalStateException e) {
            throw new DuplicateContentException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponseDto> findAllReservationTimes() {
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        return allReservationTime.stream()
                .map(ReservationTimeResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponseDto> findAvailableReservationTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        List<Reservation> availableReservationsByDate = reservationRepository.findByDateAndThemeId(date, themeId);

        List<ReservationTime> availableReservationTimes = availableReservationsByDate.stream()
                .map(Reservation::getTime)
                .toList();

        return allReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponseDto(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        availableReservationTimes.contains(reservationTime)
                ))
                .toList();
    }

    public void deleteReservationTimeById(final Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalStateException("[ERROR] 이 시간의 예약이 이미 존재합니다. id : " + id);
        }

        if (!reservationTimeRepository.existsById(id)) {
            throw new NotFoundException("[ERROR] 등록된 시간만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        reservationTimeRepository.deleteById(id);
    }
}
