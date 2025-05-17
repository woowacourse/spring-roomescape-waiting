package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.dto.AvailableReservationTimeResponseDto;
import roomescape.reservationtime.domain.dto.ReservationTimeRequestDto;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.reservationtime.exception.AlreadyReservedTimeException;
import roomescape.reservationtime.exception.DuplicateReservationTimeException;
import roomescape.reservationtime.exception.NotFoundReservationTimeException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.InvalidThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.domain.User;

@Service
@Transactional(readOnly = true)
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
        Set<ReservationTime> reservationTimesByThemeAndDate = reservationRepository.findByThemeAndDateAndUser(theme, date,
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

    @Transactional
    public void deleteById(Long id) {
        ReservationTime reservationTime = findByIdOrThrow(id);
        if (reservationRepository.existsByReservationTime(reservationTime)) {
            throw new AlreadyReservedTimeException();
        }
        repository.deleteById(id);
    }

    @Transactional
    public ReservationTimeResponseDto add(ReservationTimeRequestDto requestDto) {
        ReservationTime reservationTime = convertToReservationTimeRequestDto(requestDto);
        try {
            ReservationTime savedReservationTime = repository.save(reservationTime);
            return convertToReservationTimeResponseDto(savedReservationTime);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationTimeException();
        }
    }
    private ReservationTime convertToReservationTimeRequestDto(ReservationTimeRequestDto requestDto) {
        return requestDto.toEntity();
    }

    private ReservationTimeResponseDto convertToReservationTimeResponseDto(ReservationTime reservationTime) {
        return ReservationTimeResponseDto.of(reservationTime);
    }

    private ReservationTime findByIdOrThrow(Long id) {
        return  repository.findById(id)
                .orElseThrow(NotFoundReservationTimeException::new);
    }
}
