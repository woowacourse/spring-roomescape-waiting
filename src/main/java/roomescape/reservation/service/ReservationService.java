package roomescape.reservation.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.domain.dto.SearchReservationRequestDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.dto.ReservationRequestDto;
import roomescape.reservation.domain.dto.ReservationResponseDto;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.InvalidReservationTimeException;
import roomescape.reservation.exception.NotFoundReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.InvalidThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.domain.User;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository repository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository repository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.repository = repository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponseDto> findAll() {
        List<Reservation> reservations = repository.findAll();
        return reservations.stream()
                .map(this::convertReservationResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponseDto add(ReservationRequestDto requestDto, User user) {
        Reservation reservation = convertReservation(requestDto, user);
        validateDuplicateDateTime(reservation);
        Reservation savedReservation = repository.save(reservation);
        return convertReservationResponseDto(savedReservation);
    }

    @Transactional
    public void deleteById(Long id) {
        findByIdOrThrow(id);
        repository.deleteById(id);
    }

    private Reservation findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundReservationException("해당 예약 id가 존재하지 않습니다."));
    }

    private void validateDuplicateDateTime(Reservation inputReservation) {
        boolean exists = repository.existsByDateAndReservationTime(
                inputReservation.getDate(),
                inputReservation.getReservationTime()
        );
        if (exists) {
            throw new DuplicateReservationException();
        }
    }

    public List<ReservationResponseDto> findReservationsByUserAndThemeAndFromAndTo(
            SearchReservationRequestDto searchReservationRequestDto) {
        List<Reservation> reservations = repository.findReservationsByUserAndThemeAndFromAndTo(
                searchReservationRequestDto.userId(),
                searchReservationRequestDto.themeId(),
                searchReservationRequestDto.from(),
                searchReservationRequestDto.to()
                );

        return reservations.stream()
                .map(this::convertReservationResponseDto)
                .toList();
    }

    private Reservation convertReservation(ReservationRequestDto dto, User user) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(InvalidReservationTimeException::new);
        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(InvalidThemeException::new);

        return dto.toEntity(reservationTime, theme, user);
    }

    private ReservationResponseDto convertReservationResponseDto(Reservation reservation) {
        return ReservationResponseDto.of(reservation);
    }
}
