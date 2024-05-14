package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.member.model.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.SaveReservationRequest;
import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.dto.SearchReservationsRequest;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> searchReservations(final SearchReservationsRequest request) {
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(
                request.memberId(),
                request.themeId(),
                request.from(),
                request.to()
        );

        return reservationRepository.searchReservations(searchReservationsParams);
    }

    public Reservation saveReservation(final SaveReservationRequest request) {
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NoSuchElementException("해당 id의 예약 시간이 존재하지 않습니다."));
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NoSuchElementException("해당 id의 테마가 존재하지 않습니다."));
        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NoSuchElementException("해당 id의 회원이 존재하지 않습니다."));

        final Reservation reservation = request.toReservation(reservationTime, theme, member);
        validateReservationDateAndTime(reservation.getDate(), reservationTime);
        validateReservationDuplication(reservation);

        return reservationRepository.save(reservation);
    }

    private static void validateReservationDateAndTime(final ReservationDate date, final ReservationTime time) {
        final LocalDateTime reservationLocalDateTime = LocalDateTime.of(date.value(), time.getStartAt());
        if (reservationLocalDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 날짜보다 이전 날짜를 예약할 수 없습니다.");
        }
    }

    private void validateReservationDuplication(final Reservation reservation) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(
                reservation.getDate().value(),
                reservation.getTime().getId(),
                reservation.getTheme().getId())
        ) {
            throw new IllegalArgumentException("이미 해당 날짜/시간의 테마 예약이 있습니다.");
        }
    }

    public void deleteReservation(final Long reservationId) {
        final int deletedDataCount = reservationRepository.deleteById(reservationId);

        if (deletedDataCount <= 0) {
            throw new NoSuchElementException("해당 id의 예약이 존재하지 않습니다.");
        }
    }
}
