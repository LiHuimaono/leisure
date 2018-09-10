package com.github.hicolors.leisure.common.framework.springmvc.enhance;

import com.github.hicolors.leisure.common.utils.DingTalkUtils;
import com.github.hicolors.leisure.common.utils.Warning;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DingtalkHandler implements ErrorSourceHandler {

    private static final String DEFAULT_DINGTALK_VALUE = "none";
    /**
     * 服务中 配置的钉钉告警地址
     */
    @Value("${warning.dingtalk:none}")
    private String webhook;

    @Override
    public boolean support(ErrorSource t) {
        return t.getData() instanceof Warning;

    }

    @Override
    public void dispose(ErrorSource t) {
        if (DEFAULT_DINGTALK_VALUE.equals(webhook)) {
            log.info("服务中未配置钉钉告警机器人地址");
        } else {
            Warning warning = (Warning) t.getData();
            DingTalkUtils.send(webhook, warning);
        }
    }
}