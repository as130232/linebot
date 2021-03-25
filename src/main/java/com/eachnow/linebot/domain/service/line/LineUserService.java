package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.db.po.LineGroupUserPO;
import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.db.repository.LineGroupUserRepository;
import com.eachnow.linebot.common.db.repository.LineUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class LineUserService {
    private LineUserRepository lineUserRepository;
    private LineGroupUserRepository lineGroupUserRepository;

    @Autowired
    public LineUserService(LineUserRepository lineUserRepository,
                           LineGroupUserRepository lineGroupUserRepository) {
        this.lineUserRepository = lineUserRepository;
        this.lineGroupUserRepository = lineGroupUserRepository;
    }

    public void saveLineGroupAndUser(String userId, String groupId) {
        //先新增用戶
        this.saveLineUser(userId);
        //在新增群組關聯
        this.saveLineGroupUser(userId, groupId);
    }

    public void saveLineUser(String userId) {
        Optional<LineUserPO> optional = lineUserRepository.findById(userId);
        if (optional.isPresent()) {
            log.error("該line用戶已存在! userId:{}", userId);
            return;
        }
        lineUserRepository.save(LineUserPO.builder().id(userId).createTime(new Timestamp(Instant.now().toEpochMilli())).build());
        log.info("新增line user，成功。userId:{}", userId);
    }

    public void saveLineGroupUser(String userId, String groupId) {
        Optional<LineGroupUserPO> optional = lineGroupUserRepository.findByUserIdAndGroupId(userId, groupId);
        if (optional.isPresent()) {
            log.error("該line群組對應用戶已存在! userId:{}, groupId:{}", userId, groupId);
            return;
        }
        lineGroupUserRepository.save(LineGroupUserPO.builder().userId(userId).groupId(groupId).createTime(new Timestamp(Instant.now().toEpochMilli())).build());
        log.info("新增line group user，成功。userId:{}, groupId:{}", userId, groupId);
    }

    public void removeLineGroupUser(String userId, String groupId) {
        Optional<LineGroupUserPO> optional = lineGroupUserRepository.findByUserIdAndGroupId(userId, groupId);
        if (optional.isPresent()) {
            lineGroupUserRepository.delete(optional.get());
            log.info("移除line group user，成功。userId:{}, groupId:{}", userId, groupId);
        }
        log.error("找不到該line群組對應用戶! userId:{}, groupId:{}", userId, groupId);
    }

}
