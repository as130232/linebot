package com.eachnow.linebot.common.db.repository;

import com.eachnow.linebot.common.db.po.LineUserPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineUserRepository extends JpaRepository<LineUserPO, String> {
}
