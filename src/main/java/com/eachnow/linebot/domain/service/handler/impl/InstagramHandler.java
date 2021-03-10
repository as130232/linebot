package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.InstagramParameter;
import com.eachnow.linebot.common.po.ig.Graphql;
import com.eachnow.linebot.common.po.ig.User;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;

@Slf4j
@Command({"ig"})
public class InstagramHandler implements CommandHandler {
    private static final String INSTAGRAM_BASE_URI = "https://www.instagram.com/";

    @Override
    public Message execute(String parameters) {
        String[] paramArr = parameters.split(" ");
        if (paramArr.length == 1)
            return null;
        String account = paramArr[1];
        User user = getAccountInfo(account);
        String content = String.format("貼文：%d, 粉絲：%d\n%s", user.getEdgeOwnerToTimelineMedia().getCount(),
                user.getEdgeFollowedBy().getCount(), user.getBiography());
        log.info("444");
        if (content.length() > 60) {
            content = content.substring(0, 57) + "...";
        }
        log.info("用戶user info:{}", user);
        String title = StringUtils.isBlank(user.getFullName()) ? user.getUsername() : user.getFullName();
        URI uri = URI.create(INSTAGRAM_BASE_URI + account);
        ButtonsTemplate template = new ButtonsTemplate(URI.create(user.getProfilePicUrlHd()), title, content,
                Arrays.asList(new PostbackAction("最新貼文", "IG " + account + " " + InstagramParameter.RECENT.getValue()),
                        new PostbackAction("精選貼文", "IG " + account + " " + InstagramParameter.COLLECTION.getValue()),
                        new URIAction("IG", uri, new URIAction.AltUri(uri))));
        return new TemplateMessage(user.getFullName() + " " + user.getBiography(), template);
    }


    private User getAccountInfo(String account) {
        Document document;
        try {
            document = Jsoup.connect(INSTAGRAM_BASE_URI + account).get();
            log.info("連線成功。");
        } catch (IOException e) {
            log.error("error msg:{}", e.getMessage());
            return null;
        }

        String html = document.toString();
        int index = html.indexOf("\"entry_data\":");
        String json = html.substring(index + "\"entry_data\":".length(), html.indexOf(",\"hostname\"", index));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node;
        try {
            node = mapper.readTree(json);
            log.info("111");
        } catch (IOException e) {
            log.error("error msg:{}", e.getMessage());
            return null;
        }

        Iterator<JsonNode> iterator = node.get("ProfilePage").elements();
        log.info("iterator:{}", iterator);
        if (iterator.hasNext()) {
            JsonNode graphqlNode = iterator.next().get("graphql");
            log.info("222");
            if (graphqlNode == null) {
                log.warn("Parse \"graphql\" error");
                return null;
            }
            Graphql graphql;
            try {
                graphql = mapper.readValue(graphqlNode.toString(), Graphql.class);
                log.info("333");
            } catch (IOException e) {
                log.error("error msg:{}", e.getMessage());
                return null;
            }
            return graphql.getUser();
        } else {
            log.warn("Parse \"graphql\" error");
            return null;
        }
    }
}
