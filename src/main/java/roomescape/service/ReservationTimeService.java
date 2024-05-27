package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.dto.BookResponse;
import roomescape.domain.dto.BookResponses;
import roomescape.domain.dto.ReservationTimeRequest;
import roomescape.domain.dto.ReservationTimeResponse;
import roomescape.domain.dto.ReservationTimeResponses;
import roomescape.exception.DeleteNotAllowException;
import roomescape.exception.DuplicateNotAllowException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponses findAll() {
        final List<ReservationTimeResponse> reservationTimeRespons = reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return new ReservationTimeResponses(reservationTimeRespons);
    }

    @Transactional
    public ReservationTimeResponse create(ReservationTimeRequest reservationTimeRequest) {
        validateDuplicatedTime(reservationTimeRequest);
        final ReservationTime reservationTime = reservationTimeRepository.save(reservationTimeRequest.toEntity());
        return ReservationTimeResponse.from(reservationTime);
    }

    private void validateDuplicatedTime(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeRequest.startAt())) {
            throw new DuplicateNotAllowException("이미 등록된 시간입니다");
        }
    }

    @Transactional
    public void delete(Long id) {
        validateExistReservation(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateExistReservation(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new DeleteNotAllowException("예약이 등록된 시간은 제거할 수 없습니다.");
        }
    }

    public BookResponses findAvailableBookList(final LocalDate date, final Long themeId) {
        List<ReservationTime> reservedReservationTimes = reservationTimeRepository
                .findByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        final List<BookResponse> bookResponses = reservationTimes.stream()
                .map(reservationTime -> getBookResponse(reservationTime, reservedReservationTimes))
                .toList();
        return new BookResponses(bookResponses);
    }

    private BookResponse getBookResponse(final ReservationTime reservationTime,
                                         final List<ReservationTime> reservedReservationTimes) {
        final Boolean alreadyBooked = reservedReservationTimes.stream()
                .anyMatch(reservedTime -> reservedTime.getId().equals(reservationTime.getId()));
        return new BookResponse(reservationTime.getStartAt(), reservationTime.getId(), alreadyBooked);
    }
}
