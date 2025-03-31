package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_Id(long bookerId, Sort sort);

    List<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsAfter(long bookerId, LocalDateTime start,
                                                                  LocalDateTime end, Sort sort);

    List<Booking> findAllByBooker_IdAndEndIsBefore(long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findAllByBooker_IdAndStartIsAfter(long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findAllByBooker_IdAndStatus(long bookerId, BookingStatus status, Sort sort);

    @Query("select b from Booking b join fetch b.item as i join fetch i.owner as o where o.id = :ownerId")
    List<Booking> findAllByOwner(@Param("ownerId") long ownerId, Sort sort);

    Booking findByItem_IdAndStartIsBeforeOrderByStartDesc(long itemId, LocalDateTime start);

    Booking findByItem_IdAndEndIsAfterOrderByEnd(long itemId, LocalDateTime end);
}
