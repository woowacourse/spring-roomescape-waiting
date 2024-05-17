package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> filterReservation(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("아이디가 %s인 테마가 존재하지 않습니다.".formatted(themeId)));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("아이디가 %s인 사용자가 존재하지 않습니다.".formatted(memberId)));
        return reservationRepository.findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);
    }

    public Reservation addReservation(ReservationRequest request, Member member) {
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId(),
                request.themeId());
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 테마가 존재하지 않습니다.".formatted(request.themeId())));
        Reservation reservation = new Reservation(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public Reservation addReservation(AdminReservationRequest request) {
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId(), request.themeId());
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 테마가 존재하지 않습니다.".formatted(request.themeId())));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 사용자가 존재하지 않습니다.".formatted(request.memberId())));
        Reservation reservation = new Reservation(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    private ReservationTime findReservationTime(LocalDate date, long timeId, long themeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 예약 시간이 존재하지 않습니다.".formatted(timeId)));
        validateReservationDateTimeBeforeNow(date, reservationTime.getStartAt());
        validateDuplicatedReservation(date, themeId, timeId);
        return reservationTime;
    }

    private void validateReservationDateTimeBeforeNow(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("현재(%s) 이전 시간으로 예약할 수 없습니다.".formatted(now));
        }
    }

    private void validateDuplicatedReservation(LocalDate date, Long themeId, Long timeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("아이디가 %s인 예약 시간이 존재하지 않습니다.".formatted(timeId)));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("아이디가 %s인 테마가 존재하지 않습니다.".formatted(themeId)));
        boolean exists = reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme);
        if (exists) {
            throw new DuplicatedException("이미 해당 시간에 예약이 존재합니다.");
        }
    }

    public void deleteReservation(long id) {
        validateExistReservation(id);
        reservationRepository.deleteById(id);
    }

    private void validateExistReservation(long id) {
        boolean exists = reservationRepository.existsById(id);
        if (!exists) {
            throw new NotFoundException("해당 id:[%s] 값으로 예약된 내역이 존재하지 않습니다.".formatted(id));
        }
    }

    public List<Reservation> findMemberReservations(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NotFoundException("해당 id:[%s] 값으로 예약된 내역이 존재하지 않습니다.".formatted(memberId)));
        return reservationRepository.findAllByMember(member);
    }
}
