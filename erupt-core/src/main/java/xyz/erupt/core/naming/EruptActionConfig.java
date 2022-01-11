package xyz.erupt.core.naming;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import xyz.erupt.annotation.sub_erupt.Action;
import xyz.erupt.annotation.sub_erupt.ModalButton;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng date 2021/5/7 10:28
 */
@Component
public class EruptActionConfig implements EruptRecordOperate.DynamicConfig {

    @Resource
    private HttpServletRequest request;

    @Override
    public String naming(String desc, String eruptName, Method method) {
        EruptModel erupt = EruptCoreService.getErupt(eruptName);
        String[] code = request.getServletPath().split(EruptDataController.ACTION_PATH )[1].split(EruptDataController.SUB_ACTION_PATH);
        Action action = findAction(erupt,code[0]);
        ModalButton button = findButton(erupt,action,code[1]);
        return erupt.getErupt().name() + " | " + action.text()+" | "+button.label();
    }

    private Action findAction(EruptModel eruptModel,String code) {
        
        return Arrays.stream(eruptModel.getErupt().actions()).filter(action -> action.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(eruptModel.getEruptName() + " Action not found " + code));
    }

    private ModalButton findButton(EruptModel em,Action action,String code) {
        return Arrays.stream(action.buttons()).filter(button -> button.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(em.getEruptName() + " ModalButton not found " + code));
    }
}