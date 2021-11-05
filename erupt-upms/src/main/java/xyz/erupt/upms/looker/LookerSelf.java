package xyz.erupt.upms.looker;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.upms.helper.HyperModelCreatorVo;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/10 11:30
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

    @Override
    public String beforeFetch(List<Condition> conditions) {
        conditions.clear();
        if (eruptUserService.getCurrentEruptUser().getIsAdmin()) {
            return null;
        }
        return eruptContextService.getContextEruptClass().getSimpleName() + ".createUser.id = " + eruptUserService.getCurrentUid();
    }
}
