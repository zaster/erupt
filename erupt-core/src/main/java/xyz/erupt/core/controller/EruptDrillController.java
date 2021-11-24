package xyz.erupt.core.controller;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.naming.EruptOperateConfig;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;

/**
 * @author YuePeng date 2020-03-06
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA)
@RequiredArgsConstructor
public class EruptDrillController {

    private final EruptModifyController eruptModifyController;

    private final EruptService eruptService;

    @PostMapping("{erupt}/drill/{code}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Page drill(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
            @PathVariable("id") String id, @RequestBody TableQueryVo tableQueryVo) throws IllegalAccessException {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Link link = findDrillLink(eruptModel.getErupt(), code);
        eruptService.verifyIdPermissions(eruptModel, id);
        Object data = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel,
                EruptUtil.toEruptId(eruptModel, id));
        Object val = ReflectUtil.findFieldChain(link.column(), data);
        if (null == val)
            return new Page();
        return eruptService.getEruptData(EruptCoreService.getErupt(link.linkErupt().getSimpleName()), tableQueryVo,
                null, String.format("%s = '%s'", link.linkErupt().getSimpleName() + "." + link.joinColumn(), val));
    }

    @PostMapping("/add/{erupt}/drill/{code}/{id}")
    @EruptRecordOperate(value = "新增", dynamicConfig = EruptOperateConfig.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel drillAdd(@PathVariable("erupt") String erupt, @PathVariable("code") String code,
            @PathVariable("id") String id, @RequestBody JSONObject data, HttpServletRequest request) throws Exception {
        EruptModel<Object> eruptModel = EruptCoreService.getErupt(erupt);
        Link link = findDrillLink(eruptModel.getErupt(), code);
        eruptService.verifyIdPermissions(eruptModel, id);
        JSONObject jo = new JSONObject();
        String joinColumn = link.joinColumn();
        Field field = ReflectUtil.findClassField(eruptModel.getClazz(), link.column());
        Object val= PropertyUtils.getProperty(DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
        .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id)), field.getName());
        if (joinColumn.contains(".")) {
            String[] jc = joinColumn.split("\\.");
            JSONObject jo2 = new JSONObject();
            jo2.put(jc[1], val.toString());
            jo.put(jc[0], jo2);
        } else {
            jo.put(joinColumn, val.toString());
        }
        return eruptModifyController.addEruptData(link.linkErupt().getSimpleName(), data, jo, request);
    }

    private Link findDrillLink(Erupt erupt, String code) {
        return Stream.of(erupt.drills()).filter(it -> code.equals(it.code())).findFirst()
                .orElseThrow(EruptNoLegalPowerException::new).link();
    }

}
