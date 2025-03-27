package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.LineGroupUserPO;
import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.db.repository.LineGroupUserRepository;
import com.eachnow.linebot.common.db.repository.LineUserRepository;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.config.LineConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LineUserService {
    private final LineConfig lineConfig;
    private final LineUserRepository lineUserRepository;
    private final LineGroupUserRepository lineGroupUserRepository;

    @Autowired
    public LineUserService(LineConfig lineConfig,
                           LineUserRepository lineUserRepository,
                           LineGroupUserRepository lineGroupUserRepository) {
        this.lineConfig = lineConfig;
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
        lineUserRepository.save(LineUserPO.builder().id(userId).isValid(CommonConstant.VALID).createTime(new Timestamp(Instant.now().toEpochMilli())).build());
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

    public void updateNotifyToken(String userId, String token) {
        Optional<LineUserPO> optional = lineUserRepository.findById(userId);
        LineUserPO po;
        if (optional.isPresent()) {
            po = optional.get();
        } else {
            log.error("該用戶還未註冊! userId:{}", userId);
            po = LineUserPO.builder().id(userId).createTime(DateUtils.getCurrentTime()).build();
        }
        po.setNotifyToken(token);
        lineUserRepository.save(po);
        log.info("更新用戶notify token，完成。userId:{}, token:{}", userId, token);
    }

    public String getNotifyToken(String userId) {
        Optional<LineUserPO> optional = lineUserRepository.findById(userId);
        return optional.map(LineUserPO::getNotifyToken).orElse(null);
        //        return lineConfig.getLineNotifyKeyOwn();
    }

    public String getName(String userId) {
        Optional<LineUserPO> optional = lineUserRepository.findById(userId);
        return optional.map(LineUserPO::getName).orElse(null);
    }

    public List<LineUserPO> listUser() {
        return lineUserRepository.findAll();
    }

    public Optional<LineUserPO> getUser(String userId) {
        return lineUserRepository.findById(userId);
    }

    public void updateFemasToken(String userId, String femasToken) {
        Optional<LineUserPO> optional = getUser(userId);
        if (optional.isPresent()) {
            LineUserPO user = optional.get();
            user.setFemasToken(femasToken);
            lineUserRepository.save(user);
            log.info("updateFemasToken success. userId:{}, femasToken:{}", userId, femasToken);
        }
    }
}
