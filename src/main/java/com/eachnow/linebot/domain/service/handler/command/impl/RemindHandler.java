package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.eachnow.linebot.domain.service.schedule.quartz.QuartzService;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Command({"remind", "提醒"})
public class RemindHandler implements CommandHandler {

    private final String CONFIRM = "@remindConfirm";
    private final String CANCEL = "@remindCancel";

    private RemindRepository remindRepository;
    private QuartzService quartzService;

    @Autowired
    public RemindHandler(RemindRepository remindRepository,
                         QuartzService quartzService) {
        this.remindRepository = remindRepository;
        this.quartzService = quartzService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        //提醒 繳房租 ??????15 0800
        String label = ParamterUtils.getValueByIndex(commandPO.getParams(), 0);
        String date = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        String time = ParamterUtils.getValueByIndex(commandPO.getParams(), 2);
        if (label == null || date == null || time == null) {
            return new TextMessage("請輸入正確格式:提醒 {標頭} {日期} {時間}，例:提醒 繳房租 $$$$$$15 0800，注意需空格隔開！");
        }
        String cron = getCron(date, time);
        Integer type = getType(cron);
        if (cron == null)
            return new TextMessage("請輸入正確格式:提醒 {標頭} {日期} {時間}，例:提醒 繳房租 $$$$$$15 0800，注意需空格隔開！");

        if (commandPO.getText().contains(CONFIRM)) {
            RemindPO remindPO = RemindPO.builder().userId(commandPO.getUserId()).label(label).cron(cron)
                    .valid(CommonConstant.VALID).type(type).createTime(DateUtils.getCurrentTime()).build();
            remindPO = remindRepository.save(remindPO);
            log.info("新增提醒任務，成功。remindPO:{}", remindPO);
            //新增任務
            quartzService.addJob(remindPO.getId(), commandPO.getUserId(), label, cron);
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
                Text.builder().text("類型: " + (CommonConstant.ONCE == type ? "一次性" : "持續性")).size(FlexFontSize.LG).build(),
                Text.builder().text("日期: " + date).size(FlexFontSize.LG).build(),
                Text.builder().text("時間: " + time).size(FlexFontSize.LG).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).margin(FlexMarginSize.SM).paddingAll(FlexPaddingSize.MD).build();
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("確定").data(CONFIRM).build()).build(),
                Button.builder().style(Button.ButtonStyle.SECONDARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("取消").data(CANCEL).build()).build()
        );
        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("請問輸入正確嗎?").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#29bae6").build();
        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(footerContents).spacing(FlexMarginSize.MD).build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return new FlexMessage("提醒任務確認", contents);
    }

    /**
     * 根據cron判斷該任務是一次性或持續性類型
     */
    private Integer getType(String cron) {
        Integer type = CommonConstant.ONCE;
        if (cron != null) {
            //獲取初始字串長度
            int originLength = cron.length();
            //將字元替換後取得對應長度
            int replaceLength = cron.replace("$", "").length();
            int markLength = (originLength - replaceLength);
            if (markLength > 1)
                type = CommonConstant.CONTINUOUS;
        }
        return type;
    }

    private static String getCron(String date, String time) {
        if (date == null || time == null)
            return null;
        //20210408 161800 -> 0 18 16 8 4 ? 2021
        String cron = "{second} {minute} {hour} {day} {month} ? {year}";
        String cronOfYear = parseCron(date.substring(0, 4));
        String cronOfMonth = parseCron(date.substring(4, 6));
        String cronOfDay = parseCron(date.substring(6, 8));
        String cronOfHour = parseCron(time.substring(0, 2));
        String cronOfMinute = parseCron(time.substring(2, 4));
        String cronOfSecond = parseCron(time.substring(4, 6));
        cron = cron.replace("{year}", cronOfYear).replace("{month}", cronOfMonth).replace("{day}", cronOfDay)
                .replace("{hour}", cronOfHour).replace("{minute}", cronOfMinute).replace("{second}", cronOfSecond);
        return cron;
    }

    private static String parseCron(String cron) {
        if (cron.contains("$") || cron.contains("＄")) {
            return "*";
        }
        if (cron.indexOf("0") == 0) {
            cron = cron.substring(1, cron.length());
        }
        return cron;
    }

    public static void main(String[] args) {
        System.out.println(getCron("20210408", "161800"));
        System.out.println(getCron("$$$$$$15", "080000"));
        System.out.println(getCron("$$$$$$08", "$$1800"));
    }

}
