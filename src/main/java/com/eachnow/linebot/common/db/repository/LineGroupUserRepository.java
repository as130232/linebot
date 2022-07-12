package com.eachnow.linebot.common.db.repository;

import com.eachnow.linebot.common.db.po.LineGroupUserPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineGroupUserRepository extends JpaRepository<LineGroupUserPO, Integer> {
    Optional<LineGroupUserPO> findByUserIdAndGroupId(String userId, String groupId);
}
