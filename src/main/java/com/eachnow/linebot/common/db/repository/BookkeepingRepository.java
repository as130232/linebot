package com.eachnow.linebot.common.db.repository;

import com.eachnow.linebot.common.db.po.BookkeepingPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.query.Param;

import javax.persistence.TemporalType;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface BookkeepingRepository extends JpaRepository<BookkeepingPO, Integer> {

    @Query(value = "SELECT * FROM bookkeeping WHERE user_id = :userId AND create_time BETWEEN :startDateTime AND :endDateTime", nativeQuery = true)
    List<BookkeepingPO> findByUserIdAndCreateTimeBetween(@Param("userId") String userId,
                                                         @Param("startDateTime") Timestamp startDateTime,
                                                         @Param("endDateTime") Timestamp endDateTime);

    @Query(value = "SELECT * FROM bookkeeping WHERE user_id = :userId AND date BETWEEN :startDateTime AND :endDateTime", nativeQuery = true)
    List<BookkeepingPO> findByUserIdAndDateBetween(@Param("userId") String userId,
                                                         @Param("startDateTime") String startDateTime,
                                                         @Param("endDateTime") String endDateTime);
}
