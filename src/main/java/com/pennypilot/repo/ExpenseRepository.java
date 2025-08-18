package com.pennypilot.repo;

import com.pennypilot.model.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Finds an expense by its ID and the profile ID.
     //@param id the ID of the expense
     * @param profileId the ID of the profile
     * @return an Expense object if found, otherwise null
     */
    List<Expense> findAllByProfileIdOrderByDateDesc(Long profileId);

    /**
     * Finds top 5 profile ID ordered by date desc.
     * @param profileId
     * @return
     */
    List<Expense> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    /**
     * Finds the total expenses for a given profile ID.
     *
     * @param profileId the ID of the profile
     * @return the total amount of expenses as a BigDecimal
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpensesByProfileId(@Param("profileId") Long profileId);

    /**
     * Finds expenses by profile ID, date range, and name containing a specific string, ordered by the specified sort criteria.
     *
     * @param profileId the ID of the profile
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param name the name to search for, case-insensitive
     * @param sort the sorting criteria
     * @return a list of Expense objects matching the criteria
     */
    List<Expense> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
            Long profileId, LocalDateTime startDate, LocalDateTime endDate, String name, Sort sort
    );

    /**
     * Finds expenses by profile ID and date range.
     *
     * @param profileId the ID of the profile
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of Expense objects matching the criteria
     */
    List<Expense> findByProfileIdAndDateBetween(Long profileId, LocalDateTime startDate, LocalDateTime endDate);

}
