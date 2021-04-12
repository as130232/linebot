package com.eachnow.linebot.common.db.repository;

import com.eachnow.linebot.common.db.po.RemindPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemindRepository extends JpaRepository<RemindPO, Integer> {
    List<RemindPO> findByValid(Integer valid);
}
