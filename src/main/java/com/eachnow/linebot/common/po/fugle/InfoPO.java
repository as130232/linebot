package com.eachnow.linebot.common.po.fugle;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InfoPO {
    private String date;
    /* mode
    twse-sem: 上市
    tpex-otc: 上櫃
    tpex-emg: 興櫃
    tpex-sem-oddlot: 上市零股
    tpex-otc-oddlot: 上櫃零股 */
    private String mode;
    private String symbolId;
    private String countryCode;
    private String timeZone;
    private String lastUpdatedAt;   //2021-05-03T05:30:04.966Z
}
