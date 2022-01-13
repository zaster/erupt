package xyz.erupt.upms.looker;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.springframework.stereotype.Service;

import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.upms.helper.HyperModelCreatorVo;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 *         date 2021/3/10 11:30
 */
@MappedSuperclass
@PreDataProxy(LookerSelf.class)
@Service
public class LookerSelf extends HyperModelCreatorVo implements DataProxy<Object> {

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Resource
    @Transient
    private EruptContextService eruptContextService;
    @Transient
    @Resource
    private EruptSessionService sessionService;

    @Override
    public String beforeFetch(List<Condition> conditions) {
        conditions.clear();
        if (sessionService.getCurrentEruptUser().getIsAdmin()) {
            return null;
        }
        return eruptContextService.getContextEruptClass().getSimpleName() + ".createUser.id = "
                + sessionService.getCurrentUid();
    }
}
