package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.ReservationTimeRequestDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.exception.local.AlreadyReservedTimeException;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.InvalidThemeException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeRepository repository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(
            ReservationTimeRepository repository,
            ReservationRepository reservationRepository,
            ThemeRepository themeRepository
    ) {
        this.repository = repository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResponseDto> findAll() {
        List<ReservationTime> reservationTimes = repository.findAll();
        return reservationTimes.stream()
                .map(this::convertToReservationTimeResponseDto)
                .toList();
    }

    public List<AvailableReservationTimeResponseDto> findReservationTimesWithAvailableStatus(Long themeId,
            LocalDate date,
            User user) {
        List<ReservationTime> allTime = repository.findAll();
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(InvalidThemeException::new);
        Set<ReservationTime> reservationTimesByThemeAndDate = reservationRepository.findByThemeAndDateAndUser(theme,
                        date,
                        user)
                .stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());

        return allTime.stream()
                .map(reservationTime ->
                        AvailableReservationTimeResponseDto.from(
                                reservationTime,
                                reservationTimesByThemeAndDate.contains(reservationTime)
                        )
                )
                .toList();
    }

    public void deleteById(Long id) {
        ReservationTime reservationTime = findByIdOrThrow(id);
        if (reservationRepository.existsByReservationTime(reservationTime)) {
            throw new AlreadyReservedTimeException();
        }
        repository.deleteById(id);
    }

    public ReservationTimeResponseDto add(ReservationTimeRequestDto requestDto) {
        ReservationTime reservationTime = convertToReservationTimeRequestDto(requestDto);
        validateDuplicateTime(reservationTime);
        ReservationTime savedReservationTime = repository.save(reservationTime);
        return convertToReservationTimeResponseDto(savedReservationTime);
    }

    private void validateDuplicateTime(ReservationTime inputReservationTime) {
        boolean exists = repository.existsByStartAt(inputReservationTime.getStartAt());

        if (exists) {
            throw new DuplicateReservationException();
        }
    }

    private ReservationTime convertToReservationTimeRequestDto(ReservationTimeRequestDto requestDto) {
        return requestDto.toEntity();
    }

    private ReservationTimeResponseDto convertToReservationTimeResponseDto(ReservationTime reservationTime) {
        return ReservationTimeResponseDto.of(reservationTime);
    }

    private ReservationTime findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(NotFoundReservationTimeException::new);
    }
}
