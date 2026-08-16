package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(long ownerId, Pageable pageable);

    List<Item> findByRequestId(long requestId);

    boolean existsByOwnerId(long ownerId);

    @Query("select i from Item i "
            + "where i.available = true "
            + "and (lower(i.name) like lower(concat('%', :text, '%')) "
            + "or lower(i.description) like lower(concat('%', :text, '%')))")
    List<Item> search(@Param("text") String text, Pageable pageable);
}
