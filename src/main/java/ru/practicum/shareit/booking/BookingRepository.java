package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            long bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            long bookerId, BookingStatus status);

    @Query("select b from Booking b "
            + "where b.booker.id = :bookerId "
            + "and b.start <= :now and b.end >= :now "
            + "order by b.start desc")
    List<Booking> findCurrentByBookerId(
            @Param("bookerId") long bookerId,
            @Param("now") LocalDateTime now);

    List<Booking> findByItemOwnerIdOrderByStartDesc(long ownerId);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            long ownerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            long ownerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            long ownerId, BookingStatus status);

    @Query("select b from Booking b "
            + "where b.item.owner.id = :ownerId "
            + "and b.start <= :now and b.end >= :now "
            + "order by b.start desc")
    List<Booking> findCurrentByOwnerId(
            @Param("ownerId") long ownerId,
            @Param("now") LocalDateTime now);

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
