package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.db.po.LineGroupUserPO;
import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.db.repository.LineGroupUserRepository;
import com.eachnow.linebot.common.db.repository.LineUserRepository;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.handler.command.CommandHandlerFactory;
import com.eachnow.linebot.domain.service.handler.location.LocationHandler;
import com.eachnow.linebot.domain.service.handler.location.LocationHandlerFactory;
import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * 處理各項Line事件服務
 * Joined:加入群組
 * leave:離開群組
 * MemberJoined: 有成員加入該群組
 * MemberLeft: 有成員離開該群組
 */
@Slf4j
@LineMessageHandler
public class MessageHandler {
    private CommandHandlerFactory handlerCommandFactory;
    private LocationHandlerFactory locationHandlerFactory;
    private LineUserRepository lineUserRepository;
    private LineGroupUserRepository lineGroupUserRepository;

    @Autowired
    public MessageHandler(CommandHandlerFactory handlerCommandFactory,
                          LocationHandlerFactory locationHandlerFactory,
                          LineUserRepository lineUserRepository,
                          LineGroupUserRepository lineGroupUserRepository) {
        this.handlerCommandFactory = handlerCommandFactory;
        this.locationHandlerFactory = locationHandlerFactory;
        this.lineUserRepository = lineUserRepository;
        this.lineGroupUserRepository = lineGroupUserRepository;
    }

    public Message executeCommand(String text) {
        CommandHandler commandHandler = handlerCommandFactory.getCommandHandler(text);
        return commandHandler.execute(text);
    }

    @Transactional
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

    private void removeLineGroupUser(String userId, String groupId) {
        Optional<LineGroupUserPO> optional = lineGroupUserRepository.findByUserIdAndGroupId(userId, groupId);
        if (optional.isPresent()) {
            lineGroupUserRepository.delete(optional.get());
            log.info("移除line group user，成功。userId:{}, groupId:{}", userId, groupId);
        }
        log.error("找不到該line群組對應用戶! userId:{}, groupId:{}", userId, groupId);
    }

    /**
     * 當邀請好友、有成員加入該群組時，將該用戶userId新增至DB中
     */
    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        log.info("用戶接受邀請，handleFollowEvent，event: " + event);
        String userId = event.getSource().getUserId();
        this.saveLineUser(userId);
    }

    @EventMapping
    public void handleMemberJoinedEvent(MemberJoinedEvent event) {
        log.info("用戶加入群組，handleMemberJoinedEvent，event: " + event);
        GroupSource groupSource = (GroupSource) event.getSource();
        String userId = event.getJoined().getMembers().get(0).getUserId();
        saveLineGroupAndUser(userId, groupSource.getGroupId());
    }

    @EventMapping
    public void handleMemberLeftEvent(MemberLeftEvent event) {
        log.info("用戶已離開群組，handleMemberLeftEvent，event: " + event);
        GroupSource groupSource = (GroupSource) event.getSource();
        String userId = event.getLeft().getMembers().get(0).getUserId();
        //在新增群組關聯
        this.removeLineGroupUser(userId, groupSource.getGroupId());
    }

    /**
     * 處理文字訊息
     */
    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        log.info("userId:{}, event:{}", event.getSource().getUserId(), event);
        final String text = event.getMessage().getText();
        //根據指令取得對應指令處理服務
        return executeCommand(text);
    }

    /**
     * 處理地區訊息
     */
    @EventMapping
    public Message handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        log.info("handleLocationMessageEvent，event: " + event);
        LocationHandler locationHandler = locationHandlerFactory.getLocationHandler();
        return locationHandler.execute(event.getMessage());
    }

    @EventMapping
    public Message handlePostbackEvent(PostbackEvent event) {
        log.info("handlePostbackEvent，event: " + event);
        final String text = event.getPostbackContent().getData();
        //根據指令取得對應指令處理服務
        return executeCommand(text);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.info("handleDefaultMessageEvent，event: " + event);

    }

    /**
     * 處理圖片訊息
     */
    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
        // You need to install ImageMagick
//        handleHeavyContent(
//                event.getReplyToken(),
//                event.getMessage().getId(),
//                responseBody -> {
//                    final ContentProvider provider = event.getMessage().getContentProvider();
//                    final DownloadedContent jpg;
//                    final DownloadedContent previewImg;
//                    if (provider.isExternal()) {
//                        jpg = new DownloadedContent(null, provider.getOriginalContentUrl());
//                        previewImg = new DownloadedContent(null, provider.getPreviewImageUrl());
//                    } else {
//                        jpg = saveContent("jpg", responseBody);
//                        previewImg = createTempFile("jpg");
//                        system(
//                                "convert",
//                                "-resize", "240x",
//                                jpg.path.toString(),
//                                previewImg.path.toString());
//                    }
//                    reply(event.getReplyToken(),
//                            new ImageMessage(jpg.getUri(), previewImg.getUri()));
//                });
    }

}
