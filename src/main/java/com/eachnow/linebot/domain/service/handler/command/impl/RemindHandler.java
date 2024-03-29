package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DescriptionCommandPO;
import com.eachnow.linebot.common.po.DescriptionPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.eachnow.linebot.domain.service.schedule.quartz.QuartzService;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Command({"remind", "提醒"})
public class RemindHandler implements CommandHandler {
    private final String CONFIRM = "@remindConfirm";
    private final String CANCEL = "@remindCancel";

    private final RemindRepository remindRepository;
    private final QuartzService quartzService;
    private final LineUserService lineUserService;
    private final LineConfig lineConfig;

    @Autowired
    public RemindHandler(RemindRepository remindRepository,
                         QuartzService quartzService,
                         LineUserService lineUserService,
                         LineConfig lineConfig) {
        this.remindRepository = remindRepository;
        this.quartzService = quartzService;
        this.lineUserService = lineUserService;
        this.lineConfig = lineConfig;
    }

    private String getAuthUri(String userId) {
        String redirectUri = "https://linebotmuyu.herokuapp.com/linebot/notify/subscribe";
        String url = "https://notify-bot.line.me/oauth/authorize" +
                "?response_type=code&scope=notify&response_mode=form_post" +
                "&client_id={clientId}&redirect_uri={redirectUri}&state={state}";
        url = url.replace("{clientId}", lineConfig.getLineNotifyClientId()).replace("{redirectUri}", redirectUri).replace("{state}", userId);
        return url;
    }

    public static DescriptionPO getDescription() {
        List<DescriptionCommandPO> commands = new ArrayList<>();
        commands.add(DescriptionCommandPO.builder().explain("設定提醒").command("提醒 {標頭} {日期} {時間}").example("提醒 聖誕節 20211225").postback("提醒 聖誕節 20211225").build());
        commands.add(DescriptionCommandPO.builder().explain("查詢行事曆").command("提醒 查").postback("提醒 查").build());
        return DescriptionPO.builder().title("提醒").description("輸入有效格式{日期}yyyyMMdd、{時間}為hhMMss，若時間為持續性，則可輸入『$$』符號，例:每年每月15號都須提醒繳房租則-> 提醒 繳房租 $$$$$$15 090000。")
                .commands(commands).imageUrl("https://image.freepik.com/free-vector/little-people-characters-make-online-schedule-tablet-design-business-graphics-tasks-scheduling-week-flat-style-modern-design-illustration-web-page-cards-poster_126608-502.jpg").build();
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        //先取得notify token，若還未訂閱line notify，則導向驗證notify token
        String notifyToken = lineUserService.getNotifyToken(commandPO.getUserId());
        if (Strings.isEmpty(notifyToken)) {
            return getAuthMessage(commandPO.getUserId());
        }
        //查詢提醒
        if (text.contains("查") || text.contains("check")) {
        }
        //提醒 繳房租 $$$$$$15 0900
        String label = ParamterUtils.getValueByIndex(commandPO.getParams(), 0);
        String date = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        String time = ParamterUtils.getValueByIndex(commandPO.getParams(), 2);
        if (time == null)
            time = "000000";    //default 00:00:00
        if (time.length() == 4)
            time += "00";       //輸入省略秒數則自動補上
        if (label == null || date == null) {
            return new TextMessage("請輸入正確格式:提醒 {標頭} {日期} {時間}，例:提醒 繳房租 $$$$$$15 0900，注意需空格隔開！");
        }
        String cron = QuartzService.getCron(date, time);
        Integer type = getType(cron);
        if (cron == null)
            return new TextMessage("日期與時間格式錯誤，{日期}為yyyyMMdd、{時間}為hhMMss，例:20210101 083000，為2021年1月1日早上8點30分");

        if (commandPO.getText().contains(CONFIRM)) {
            RemindPO remindPO = RemindPO.builder().userId(commandPO.getUserId()).label(label).cron(cron)
                    .valid(CommonConstant.VALID).type(type).createTime(DateUtils.getCurrentTime()).build();
            remindPO = remindRepository.save(remindPO);
            log.info("新增提醒任務，成功。remindPO:{}", remindPO);
            //新增任務
            JobKey jobKey = quartzService.getJobKey(remindPO.getId().toString());
            quartzService.addRemindJob(jobKey, remindPO.getId(), commandPO.getUserId(), label, cron);
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());    //移除緩存
            return new TextMessage("新增提醒成功。");
        } else if (commandPO.getText().contains(CANCEL)) {
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());    //移除緩存
            return new TextMessage("新增提醒已取消。");
        }
        String data = commandPO.getCommand() + ParamterUtils.CONTACT + label + ParamterUtils.CONTACT +
                date + ParamterUtils.CONTACT + time + ParamterUtils.CONTACT;
        MessageHandler.setUserAndCacheCommand(commandPO.getUserId(), data); //新增緩存
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text("標頭: " + label).size(FlexFontSize.LG).build(),
                Text.builder().text("類型: " + (CommonConstant.ONCE.equals(type) ? "一次性" : "持續性")).size(FlexFontSize.LG).build(),
                Text.builder().text("日期: " + parseDateByCron(cron)).size(FlexFontSize.LG).build(),
                Text.builder().text("時間: " + parseTimeByCron(cron)).size(FlexFontSize.LG).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).margin(FlexMarginSize.SM).paddingAll(FlexPaddingSize.MD).build();
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("確定").data(CONFIRM).build()).build(),
                Button.builder().style(Button.ButtonStyle.SECONDARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("取消").data(CANCEL).build()).build()
        );
        List<FlexComponent> headerContents = Collections.singletonList(Text.builder().text("請問輸入正確嗎?").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#29bae6").build();
        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(footerContents).spacing(FlexMarginSize.MD).build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return new FlexMessage("提醒任務確認", contents);
    }

    private Message getAuthMessage(String userId) {
        URI pictureUri = URI.create("https://image.freepik.com/free-vector/mobile-phones-smartphones-with-push-notification-bubbles-flat-cartoon-illustration_101884-813.jpg");
        Image hero = Image.builder().size(Image.ImageSize.FULL_WIDTH).aspectRatio(2, 2).aspectMode(Image.ImageAspectMode.Cover).url(pictureUri).build();
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text("需要主人授權同意，才可設定提醒").align(FlexAlign.CENTER).build(),
                Text.builder().text("點選訂閱並選擇以下").align(FlexAlign.CENTER).build(),
                Separator.builder().margin(FlexMarginSize.MD).color("#666666").build(),
                Text.builder().text("透過1對1聊天接收LINE Notify的通知").color("#ff0000").wrap(true).align(FlexAlign.CENTER).margin(FlexMarginSize.LG).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.SM).contents(bodyContents).build();
        URI authUri = URI.create(getAuthUri(userId));
        List<FlexComponent> footerContents = Collections.singletonList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).action(new URIAction("訂閱", authUri, new URIAction.AltUri(authUri))).build());
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.SM).contents(footerContents).build();
        List<Bubble> listBubble = Collections.singletonList(
                Bubble.builder().header(null).hero(hero).body(body).footer(footer).build()
        );
        FlexContainer contents = Carousel.builder().contents(listBubble).build();
        return new FlexMessage("取得權限", contents);
    }

    /**
     * 根據cron判斷該任務是一次性或持續性類型
     */
    private static Integer getType(String cron) {
        Integer type = CommonConstant.ONCE;
        if (cron != null) {
            //獲取初始字串長度
            int originLength = cron.length();
            //將字元替換後取得對應長度
            int replaceLength = cron.replace("*", "").length();
            int markLength = (originLength - replaceLength);
            if (markLength > 1)
                type = CommonConstant.CONTINUOUS;
        }
        return type;
    }

    /**
     * 解析cron 每一個時間參數 > 0 0 8 15 * ? *
     * 並轉成顯示的敘述
     */
    private static String parseCronDescription(String cronParam) {
        if (cronParam.contains("*")) {
            return "每";
        }
        return cronParam;
    }

    public static String parseDateByCron(String cron) {
        //0 0 8 15 * ? *
        String date = "{year}年 {month}月 {day}日";
        String[] cronArr = cron.split(" ");
        String year = parseCronDescription(cronArr[6]);
        String month = parseCronDescription(cronArr[4]);
        String day = parseCronDescription(cronArr[3]);
        return date.replace("{year}", year).replace("{month}", month).replace("{day}", day);
    }

    public static String parseTimeByCron(String cron) {
        //0 0 8 15 * ? *
        String time = "{hour}時 {minute}分 {second}秒";
        String[] cronArr = cron.split(" ");
        String hour = parseCronDescription(cronArr[2]);
        String minute = parseCronDescription(cronArr[1]);
        String second = parseCronDescription(cronArr[0]);
        return time.replace("{hour}", hour).replace("{minute}", minute).replace("{second}", second);
    }

}
