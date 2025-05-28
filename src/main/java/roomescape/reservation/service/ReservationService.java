package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.repository.WaitingRepository;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberQueryService memberQueryService;
    private final WaitingRepository waitingRepository;

    @Transactional
    public ReservationResponse reserve(ReserveCommand command) {
        LocalDate date = command.date();
        Long timeId = command.timeId();
        Long themeId = command.themeId();

        isAlreadyReserved(date, timeId, themeId);

        ReservationDateTime reservationDateTime = ReservationDateTime.create(
                new ReservationDate(date), reservationTimeService.getReservationTime(timeId));
        Theme theme = themeService.getTheme(themeId);
        Member reserver = memberQueryService.getMember(command.memberId());

        LocalDateTime reservedAt = LocalDateTime.now();
        Reservation reservation = Reservation.reserve(reserver, reservationDateTime, theme, reservedAt);
        Reservation saved = reservationRepository.save(reservation);

        return ReservationResponse.from(saved);
    }

    private void isAlreadyReserved(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsBy(date, timeId, themeId)) {
            throw new InvalidArgumentException("이미 예약했습니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> getFilteredReservations(
            Long themeId,
            Long memberId,
            LocalDate from,
            LocalDate to
    ) {
        if (themeId == null && memberId == null && from == null && to == null) {
            return getAllReservations();
        }

        List<Reservation> reservations = reservationRepository.findFilteredReservations(
                themeId, memberId, from, to
        );

        return ReservationResponse.from(reservations);
    }

    private List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return ReservationResponse.from(reservations);
    }

    public List<MyReservationResponse> getMyReservations(Long memberId) {
        return Stream.concat(
                        reservationRepository.findByMemberId(memberId)
                                .stream()
                                .map(MyReservationResponse::from),
                        waitingRepository.findWaitingWithRankByMemberId(memberId)
                                .stream()
                                .map(MyReservationResponse::from)
                )
                .toList();
    }
}
