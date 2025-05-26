package roomescape.service.reservation;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationCreator;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.*;
import roomescape.service.dto.ReservationCreateDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationCommandService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;
    private final JpaWaitingRepository waitingRepository;
    private final ReservationCreator reservationCreator;

    public ReservationCommandService(JpaReservationRepository reservationRepository,
                                     JpaReservationTimeRepository reservationTimeRepository,
                                     JpaThemeRepository themeRepository,
                                     JpaMemberRepository memberRepository,
                                     JpaWaitingRepository waitingRepository,
                                     ReservationCreator reservationCreator) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
        this.reservationCreator = reservationCreator;
    }

    public ReservationResponseDto createReservation(ReservationCreateDto dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + dto.memberId()));

        Reservation reservation = reservationCreator.create(member, dto.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(reservation);

        return ReservationResponseDto.of(newReservation, newReservation.getTime(), theme);
    }

    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다."));

        Theme theme = reservation.getTheme();
        LocalDate date = reservation.getDate();
        ReservationTime time = reservation.getTime();

        reservationRepository.delete(reservation);

        promoteWaitingIfExists(theme, date, time);
    }

    private void promoteWaitingIfExists(Theme theme, LocalDate date, ReservationTime time) {
        Optional<Waiting> firstWaiting = waitingRepository.findWaitingsFor(
                theme, date, time, PageRequest.of(0, 1))
                .stream()
                .findFirst();

        if (firstWaiting.isEmpty()) return;

        Waiting waiting = firstWaiting.get();
        Member member = waiting.getMember();

        Reservation newReservation = Reservation.createWithoutId(member, date, time, theme);
        reservationRepository.save(newReservation);

        waitingRepository.delete(waiting);
    }
}
