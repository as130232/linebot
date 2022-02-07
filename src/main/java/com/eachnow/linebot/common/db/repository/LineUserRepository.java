package com.eachnow.linebot.common.db.repository;

import com.eachnow.linebot.common.db.po.LineUserPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineUserRepository extends JpaRepository<LineUserPO, String> {
    Optional<LineUserPO> findByCode(String command);
}
