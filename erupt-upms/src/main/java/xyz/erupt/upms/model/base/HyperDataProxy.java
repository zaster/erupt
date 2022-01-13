package xyz.erupt.upms.model.base;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 *         date 2020-08-04
 */
@Service
public class HyperDataProxy implements DataProxy<HyperModel> {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptSessionService sessionService;

    @Override
    public void beforeAdd(HyperModel hyperModel) {
        hyperModel.setCreateTime(new Date());
        hyperModel.setCreateUser(new EruptUserVo(sessionService.getCurrentUid()));
    }

    @Override
    public void beforeUpdate(HyperModel hyperModel) {
        hyperModel.setUpdateTime(new Date());
        hyperModel.setUpdateUser(new EruptUserVo(sessionService.getCurrentUid()));
    }
}
