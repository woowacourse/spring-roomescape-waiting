package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.repository.ReservationRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.IdNotFoundException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }


    public List<ReservationOrderResponse> findByName(String name) {
        return reservationRepository.findByName(name).stream()
                .map(reservation -> ReservationOrderResponse.from(reservation, calculateOrder(reservation)))
                .toList();
    }

    public List<ReservationOrderResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservation -> ReservationOrderResponse.from(reservation, calculateOrder(reservation)))
                .toList();
    }

    @Transactional
    public ReservationResponse save(ReservationRequest request, LocalDateTime requestedAt) {
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        validateReservationDateTime(request.date(), time);

        Reservation reservation = new Reservation(request.name(), request.date(), time, theme, requestedAt);

        try {
            Reservation saved = reservationRepository.save(reservation);
            return ReservationResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationException("이미 동일한 예약 또는 대기 신청이 존재합니다.");
        }
    }

    @Transactional
    public ReservationResponse update(Long id, UserReservationUpdateRequest request) {
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        validateReservationDateTime(request.date(), time);

        if (reservationRepository.existsByDateAndTimeAndTheme(request.date(), time, theme)) {
            throw new IllegalArgumentException("요청하신 날짜 및 시간에는 예약이 존재해 변경 불가합니다. 대기를 원하신다면 취소 후 신청해주세요.");
        }

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("요청하신 예약을 찾을 수 없습니다."));
        reservation.changeSchedule(request.date(), time);   // 변경 감지(dirty checking)로 커밋 시 UPDATE
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }


    private long calculateOrder(Reservation reservation) {
        List<Reservation> sameSlot = reservationRepository.findByDateAndTime_IdAndTheme_IdOrderByRequestedAt(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId());

        return sameSlot.stream()
                .filter(other -> other.isRequestedBefore(reservation))
                .count();
    }

    private ReservationTime getValidReservationTime(Long timeId) {
        try {
            return reservationTimeRepository.findReservationTimeById(timeId);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("요청하신 시간 정보를 찾을 수 없습니다.  선택하신 시간이 정확한지 다시 한번 확인해 주세요.");
        }
    }

    private Theme getValidTheme(Long themeId) {
        try {
            return themeRepository.findThemeById(themeId);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요.");
        }
    }

    private void validateReservationDateTime(LocalDate date, ReservationTime time) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, time.getStartAt());
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이미 지난 시간/날짜는 예약할 수 없습니다.");
        }
    }
}
