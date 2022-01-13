package xyz.erupt.upms.model.log;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 *         date 2021/8/20 14:44
 */
@Component
public class AdminPower implements PowerHandler {

    @Resource
    private EruptUserService eruptUserService;
    @Resource
    private EruptSessionService sessionService;

    @Override
    public void handler(PowerObject power) {
        if (sessionService.getCurrentEruptUser().getIsAdmin()) {
            power.setDelete(true);
        }
    }
}
