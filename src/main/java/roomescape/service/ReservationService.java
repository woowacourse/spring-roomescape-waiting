package roomescape.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.dto.request.SearchReservationRequestDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.dto.response.UserResponseDto;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.InvalidReservationTimeException;
import roomescape.exception.local.InvalidThemeException;
import roomescape.exception.local.NotFoundReservationException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
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

    public ReservationResponseDto add(ReservationRequestDto requestDto, User user) {
        Reservation reservation = convertReservation(requestDto, user);
        validateDuplicateDateTime(reservation);
        Reservation savedReservation = repository.save(reservation);
        return convertReservationResponseDto(savedReservation);
    }

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
        ReservationTimeResponseDto reservationTimeResponseDto = ReservationTimeResponseDto.of(
                reservation.getReservationTime());
        ThemeResponseDto themeResponseDto = ThemeResponseDto.of(reservation.getTheme());
        UserResponseDto userResponseDto = UserResponseDto.of(reservation.getUser());
        return ReservationResponseDto.from(reservation, reservationTimeResponseDto, themeResponseDto, userResponseDto);
    }
}
