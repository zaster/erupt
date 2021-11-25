package xyz.erupt.core.controller;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.SceneEnum;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.naming.EruptOperateConfig;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;

/**
 * Erupt 对数据的增删改查
 *
 * @author YuePeng
 * date 9/28/18.
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY)
@RequiredArgsConstructor
public class EruptModifyController {
    private final Gson gson = GsonFactory.getGson();
    private final EruptService eruptService;

    @SneakyThrows
    @PostMapping({"/{erupt}"})
    @EruptRecordOperate(value = "新增", dynamicConfig = EruptOperateConfig.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data,
                JsonObject jsonObject, HttpServletRequest request) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isAdd);
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field()) && dependTree.dependNode()) {
            String linkVal = request.getHeader("link");
            //必须是强依赖才会自动注入值
            if (dependTree.dependNode()) {
                if (StringUtils.isBlank(linkVal)) {
                    return EruptApiModel.errorApi("请选择树节点");
                } else {
                    if (null == jsonObject) jsonObject = new JsonObject();
                    String rm = ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName();
                    JsonObject sub = new JsonObject();
                    sub.addProperty(EruptCoreService.getErupt(rm).getErupt().primaryKeyCol(), linkVal);
                    jsonObject.add(dependTree.field(), sub);
                }
            }
        }
        EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) return eruptApiModel;
        Object o = gson.fromJson(data, eruptModel.getClazz());
        EruptUtil.clearObjectDefaultValueByJson(o, data);
        Object obj = EruptUtil.dataTarget(eruptModel, o, eruptModel.getClazz().newInstance(), SceneEnum.ADD);
        if (null != jsonObject) {
            for (String key : jsonObject.keySet()) {
                Field field = ReflectUtil.findClassField(eruptModel.getClazz(), key);
                field.setAccessible(true);
                field.set(obj, gson.fromJson(jsonObject.get(key), field.getType()));
            }
        }
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeAdd(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, obj);
        this.modifyLog(eruptModel, "ADD", data.toString());
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterAdd(obj)));
        return EruptApiModel.successApi();
    }

    @PutMapping("/{erupt}")
    @EruptRecordOperate(value = "修改", dynamicConfig = EruptOperateConfig.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public EruptApiModel editEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) throws IllegalAccessException {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) return eruptApiModel;
        eruptService.verifyIdPermissions(eruptModel, data.get(eruptModel.getErupt().primaryKeyCol()).getAsString());
        Object o = this.gson.fromJson(data, eruptModel.getClazz());
        EruptUtil.clearObjectDefaultValueByJson(o, data);
        Object obj = EruptUtil.dataTarget(eruptModel, o, DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .findDataById(eruptModel, ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(o)), SceneEnum.EDIT);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
        this.modifyLog(eruptModel, "EDIT", data.toString());
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));
        return EruptApiModel.successApi();
    }

    @DeleteMapping("/{erupt}/{id}")
    @EruptRecordOperate(value = "删除", dynamicConfig = EruptOperateConfig.class)
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isDelete);
        eruptService.verifyIdPermissions(eruptModel, id);
        IEruptDataService dataService = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());
        //获取对象数据信息用于DataProxy函数中
        Object obj = dataService.findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeDelete(obj)));
        dataService.deleteData(eruptModel, obj);
        this.modifyLog(eruptModel, "DELETE", id);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterDelete(obj)));
        return EruptApiModel.successApi();
    }

    
    @Transactional
    @DeleteMapping("/{erupt}")
    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @EruptRecordOperate(value = "批量删除", dynamicConfig = EruptOperateConfig.class)
    public EruptApiModel deleteEruptDataList(@PathVariable("erupt") String erupt, @RequestParam("ids") String[] ids) {
        EruptApiModel eruptApiModel = EruptApiModel.successApi();
        for (String id : ids) {
            eruptApiModel = this.deleteEruptData(erupt, id);
            if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {
                break;
            }
        }
        return eruptApiModel;
    }

    private <TT>void modifyLog(EruptModel eruptModel, String placeholder, String content) {
        log.info("[" + eruptModel.getEruptName() + " -> " + placeholder + "]:" + content);
    }
}
