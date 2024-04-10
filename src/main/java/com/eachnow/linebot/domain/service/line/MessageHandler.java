package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DatetimepickerPO;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private LineUserService lineUserService;
    public static Map<String, String> userAndCacheCommand = new HashMap<>();   //緩存command通常用於command handler持續性，即使沒有包含該command字眼

    @Autowired
    public MessageHandler(CommandHandlerFactory handlerCommandFactory,
                          LocationHandlerFactory locationHandlerFactory,
                          LineUserService lineUserService) {
        this.handlerCommandFactory = handlerCommandFactory;
        this.locationHandlerFactory = locationHandlerFactory;
        this.lineUserService = lineUserService;
    }

//    @PostConstruct
//    private void test() {
//        String text = "抽 多";
//        //根據指令取得對應指令處理服務
//        Message message = executeCommand("test", text, null);
//        System.out.println(message);
//    }

    public static void setUserAndCacheCommand(String userId, String text) {
        userAndCacheCommand.put(userId, text);
    }

    public static void removeUserAndCacheCommand(String userId) {
        userAndCacheCommand.remove(userId);
    }

    public Message executeCommand(String userId, String text, DatetimepickerPO datetimepicker) {
        if (userAndCacheCommand.get(userId) != null)
            text = userAndCacheCommand.get(userId) + text;
        CommandPO commandPO = CommandPO.builder().userId(userId).text(text).datetimepicker(datetimepicker)
                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
        CommandHandler commandHandler = handlerCommandFactory.getCommandHandler(commandPO);
        return commandHandler.execute(commandPO);
    }

    /**
     * 當邀請好友、有成員加入該群組時，將該用戶userId新增至DB中
     */
    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        log.info("用戶接受邀請，handleFollowEvent，event: " + event);
        String userId = event.getSource().getUserId();
        lineUserService.saveLineUser(userId);
    }

    @EventMapping
    public void handleMemberJoinedEvent(MemberJoinedEvent event) {
        log.info("用戶加入群組，handleMemberJoinedEvent，event: " + event);
        GroupSource groupSource = (GroupSource) event.getSource();
        String userId = event.getJoined().getMembers().get(0).getUserId();
        lineUserService.saveLineGroupAndUser(userId, groupSource.getGroupId());
    }

    @EventMapping
    public void handleMemberLeftEvent(MemberLeftEvent event) {
        log.info("用戶已離開群組，handleMemberLeftEvent，event: " + event);
        GroupSource groupSource = (GroupSource) event.getSource();
        String userId = event.getLeft().getMembers().get(0).getUserId();
        //在新增群組關聯
        lineUserService.removeLineGroupUser(userId, groupSource.getGroupId());
    }

    /**
     * 處理文字訊息
     */
    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        log.info("userId:{}, event:{}", event.getSource().getUserId(), event);
        final String text = event.getMessage().getText();
        //根據指令取得對應指令處理服務
        return executeCommand(event.getSource().getUserId(), text, null);
    }

    /**
     * 處理地區訊息
     */
    @EventMapping
    public Message handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        log.info("handleLocationMessageEvent，event:{}, search:{}", event, LocationHandlerFactory.search);
        LocationHandler locationHandler = locationHandlerFactory.getLocationHandler();
        return locationHandler.execute(event.getMessage());
    }

    @EventMapping
    public Message handlePostbackEvent(PostbackEvent event) {
        log.info("handlePostbackEvent，event: " + event);
        final String text = event.getPostbackContent().getData();
        DatetimepickerPO datetimepickerPO = null;
        if (text.contains("datetimepicker")) {
            Map<String, String> datetimepickerMap = event.getPostbackContent().getParams();
            try {
                datetimepickerPO = JsonUtils.toObject(datetimepickerMap, DatetimepickerPO.class);
                if (text.contains("-"))
                    datetimepickerPO.setType(text.split("-")[1]);
            } catch (Exception e) {
                log.error("parsedDatetimepicker failed! error msg:{}", e.getMessage());
            }
        }
        //根據指令取得對應指令處理服務
        return executeCommand(event.getSource().getUserId(), text, datetimepickerPO);
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
