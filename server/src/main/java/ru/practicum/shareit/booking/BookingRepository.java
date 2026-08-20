package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(
            long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            long bookerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b "
            + "where b.booker.id = :bookerId "
            + "and b.start <= :now and b.end >= :now "
            + "order by b.start desc")
    List<Booking> findCurrentByBookerId(
            @Param("bookerId") long bookerId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(
            long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            long ownerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b "
            + "where b.item.owner.id = :ownerId "
            + "and b.start <= :now and b.end >= :now "
            + "order by b.start desc")
    List<Booking> findCurrentByOwnerId(
            @Param("ownerId") long ownerId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    Optional<Booking> findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
            long itemId, BookingStatus status, LocalDateTime start);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            long itemId, BookingStatus status, LocalDateTime start);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(
            long itemId,
            long bookerId,
            BookingStatus status,
            LocalDateTime end);
}
