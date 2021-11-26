package xyz.erupt.upms.handler;

import javax.annotation.Resource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.stereotype.Service;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.core.util.EruptAssert;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 * date 2020/12/28 22:33
 */
@Service
@Comment("通过菜单编码控制是否显示" +
         "1.6.12级以上版本请使用 ViaMenuValueCtrl 代替")
@Deprecated
public class ViaMenuCtrl implements ExprBool.ExprHandler {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    @Comment("params必填，值为菜单编码")
    public boolean handler(boolean expr, String[] params) {
        EruptAssert.notNull(params,ViaMenuCtrl.class.getSimpleName() + " → params[0] not found");
        try {
            return null != eruptUserService.getEruptMenuByCode(params[0]);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

}
