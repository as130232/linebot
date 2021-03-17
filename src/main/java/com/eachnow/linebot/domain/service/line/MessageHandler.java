package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.constant.CommandConstants;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.eachnow.linebot.domain.service.handler.CommandHandlerFactory;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
@LineMessageHandler
public class MessageHandler {
    private CommandHandlerFactory handlerCommandFactory;
    public static String command;

    @Autowired
    public MessageHandler(CommandHandlerFactory handlerCommandFactory) {
        this.handlerCommandFactory = handlerCommandFactory;
    }

    public Message executeCommand(String text) {
        CommandHandler commandHandler = handlerCommandFactory.getCommandHandler(text);
        return commandHandler.execute(text);
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
        System.out.println("handleLocationMessageEvent，event: " + event);
        LocationMessageContent locationMessageContent = event.getMessage();
        locationMessageContent.getTitle();
        locationMessageContent.getAddress();    //地址
        locationMessageContent.getLatitude();   //經度
        locationMessageContent.getLongitude();  //經度
        if (CommandConstants.LOCATION_RESTAURANT.equals(command)) {

        }
        command = null;    //清空
        return null;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("handleDefaultMessageEvent，event: " + event);

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
