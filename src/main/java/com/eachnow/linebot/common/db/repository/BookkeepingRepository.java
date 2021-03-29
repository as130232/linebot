package com.eachnow.linebot.common.db.repository;

import com.eachnow.linebot.common.db.po.BookkeepingPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookkeepingRepository extends JpaRepository<BookkeepingPO, String> {
}
