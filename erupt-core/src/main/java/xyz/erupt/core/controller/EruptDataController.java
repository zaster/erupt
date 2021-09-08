package xyz.erupt.core.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.RowOperation.EruptMode;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.CheckboxType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.naming.EruptRowOperationConfig;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.service.PreEruptDataService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.CheckboxModel;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;
import xyz.erupt.core.view.TreeModel;

/**
 * @author YuePeng date 9/28/18.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA)
@RequiredArgsConstructor
@Slf4j
public class EruptDataController {

    private final EruptService eruptService;

    private final PreEruptDataService preEruptDataService;

    private final Gson gson = GsonFactory.getGson();

    @PostMapping({ "/table/{erupt}" })
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody TableQueryVo tableQueryVo) {
        return eruptService.getEruptData(EruptCoreService.getErupt(eruptName), tableQueryVo, null);
    }

    @GetMapping("/tree/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public Collection<TreeModel> getEruptTreeData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        Tree tree = eruptModel.getErupt().tree();
        return preEruptDataService.geneTree(eruptModel, tree.id(), tree.label(), tree.pid(), tree.rootPid(),
                EruptQuery.builder().build());
    }

    // 树懒加载
    @GetMapping("/tree_load/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public Collection<TreeModel> getEruptTreeByChildren(@PathVariable("erupt") String eruptName,
            @RequestParam("pid") String pid) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        return null;
    }

    // 获取初始化数据
    @GetMapping("/init-value/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public Map<String, Object> initEruptValue(@PathVariable("erupt") String eruptName)
            throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Object obj = eruptModel.getClazz().newInstance();
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.addBehavior(obj)));
        return EruptUtil.generateEruptDataMap(eruptModel, obj);
    }

    @GetMapping("/{erupt}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Map<String, Object> getEruptDataById(@PathVariable("erupt") String eruptName,
            @PathVariable("id") String id) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isEdit() || powerObject.isViewDetails());
        eruptService.verifyIdPermissions(eruptModel, id);
        Object data = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel,
                EruptUtil.toEruptId(eruptModel, id));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.editBehavior(data)));
        return EruptUtil.generateEruptDataMap(eruptModel, data);
    }

    public static final String OPERATOR_PATH_STR = "/operator";

    @PostMapping("/{erupt}" + OPERATOR_PATH_STR + "/{code}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @EruptRecordOperate(value = "", dynamicConfig = EruptRowOperationConfig.class)
    public EruptApiModel execEruptOperator(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
            @RequestBody JsonObject body) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        JsonObject paramobj = (!body.get("param").isJsonNull()) ? body.getAsJsonObject("param") : null;
        RowOperation rowOperation = Arrays.stream(eruptModel.getErupt().rowOperation())
                .filter(it -> code.equals(it.code())).findFirst().orElseThrow(EruptNoLegalPowerException::new);
        if (!ExprInvoke.getExpr(rowOperation.show())) {
            throw new EruptNoLegalPowerException();
        }
        if (rowOperation.operationHandler().isInterface()) {
            return EruptApiModel.errorApi("请为" + rowOperation.title() + "实现 OperationHandler 接口");
        }
        if (rowOperation.eruptClass() != void.class && rowOperation.eruptMode() == EruptMode.FORM) {
            EruptModel erupt = EruptCoreService.getErupt(rowOperation.eruptClass().getSimpleName());
            EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(erupt, body.getAsJsonObject("param"));
            if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR)
                return eruptApiModel;
        }

        OperationHandler<Object, Object> operationHandler = EruptSpringUtil.getBean(rowOperation.operationHandler());
        Object param = null;
        // 表单形式参数
        if (paramobj != null && rowOperation.eruptMode() == EruptMode.FORM) {
            param = gson.fromJson(paramobj, rowOperation.eruptClass());
        }
        // 表格形式参数
        List<Object> list = new ArrayList<>();
        if (body.get("ids").isJsonArray() && body.getAsJsonArray("ids").size() > 0) {

            for (JsonElement id : body.getAsJsonArray("ids")) {
                Object obj = null;
                if (rowOperation.eruptMode() == EruptMode.FORM) {
                    obj = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel,
                            EruptUtil.toEruptId(eruptModel, id.getAsString()));
                } else {
                    EruptModel tableEruptModel = EruptCoreService.getErupt(rowOperation.eruptClass().getSimpleName());
                    obj = DataProcessorManager.getEruptDataProcessor(tableEruptModel.getClazz())
                            .findDataById(tableEruptModel, EruptUtil.toEruptId(tableEruptModel, id.getAsString()));
                }

                list.add(obj);
            }
        }
        if (list.isEmpty() && (rowOperation.mode() != RowOperation.Mode.BUTTON
                || rowOperation.eruptMode() == RowOperation.EruptMode.TABLE)) {
            return EruptApiModel.errorApi("执行该操作时请至少选中一条数据");
        }

        operationHandler.exec(list, param, rowOperation.operationParam());
        return EruptApiModel.successApi("执行成功", null);

    }

    @GetMapping("/tab/tree/{erupt}/{tabFieldName}")
    @EruptRouter(authIndex = 3, verifyType = EruptRouter.VerifyType.ERUPT)
    public Collection<TreeModel> findTabTree(@PathVariable("erupt") String eruptName,
            @PathVariable("tabFieldName") String tabFieldName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isViewDetails() || powerObject.isEdit());
        EruptModel tabEruptModel = EruptCoreService
                .getErupt(eruptModel.getEruptFieldMap().get(tabFieldName).getFieldReturnName());
        Tree tree = tabEruptModel.getErupt().tree();
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(tabFieldName);
        EruptQuery eruptQuery = EruptQuery.builder()
                .conditionStrings(
                        AnnotationUtil.switchFilterConditionToStr(eruptFieldModel.getEruptField().edit().filter()))
                .build();
        return preEruptDataService.geneTree(tabEruptModel, tree.id(), tree.label(), tree.pid(), tree.rootPid(),
                eruptQuery);
    }

    @GetMapping("/{erupt}/checkbox/{fieldName}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Collection<CheckboxModel> findCheckbox(@PathVariable("erupt") String eruptName,
            @PathVariable("fieldName") String fieldName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isViewDetails() || powerObject.isEdit());
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        EruptModel tabEruptModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
        CheckboxType checkboxType = eruptFieldModel.getEruptField().edit().checkboxType();
        List<Column> columns = new ArrayList<>();
        columns.add(new Column(checkboxType.id(), AnnotationConst.ID));
        columns.add(new Column(checkboxType.label(), AnnotationConst.LABEL));
        EruptQuery eruptQuery = EruptQuery.builder()
                .conditionStrings(
                        AnnotationUtil.switchFilterConditionToStr(eruptFieldModel.getEruptField().edit().filter()))
                .build();
        Collection<Map<String, Object>> collection = preEruptDataService.createColumnQuery(tabEruptModel, columns,
                eruptQuery);
        Collection<CheckboxModel> checkboxModels = new ArrayList<>(collection.size());
        collection.forEach(map -> checkboxModels
                .add(new CheckboxModel(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL))));
        return checkboxModels;
    }

    // REFERENCE API
    @PostMapping("/{erupt}/reference-table/{fieldName}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Page getReferenceTable(@PathVariable("erupt") String eruptName, @PathVariable("fieldName") String fieldName,
            @RequestParam(value = "dependValue", required = false) Serializable dependValue,
            @RequestParam(value = "tabRef", required = false) Boolean tabRef, @RequestBody TableQueryVo tableQueryVo) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isEdit() || powerObject.isAdd()
                || eruptFieldModel.getEruptField().edit().search().value());
        Edit edit = eruptFieldModel.getEruptField().edit();
        String dependField = edit.referenceTableType().dependField();
        String dependCondition = "";
        if (!AnnotationConst.EMPTY_STR.equals(dependField)) {
            Erupts.requireNonNull(dependCondition,
                    "请先选择" + eruptModel.getEruptFieldMap().get(dependField).getEruptField().edit().title());
            dependCondition = eruptFieldModel.getFieldReturnName() + '.' + edit.referenceTableType().dependColumn()
                    + '=' + dependValue;
        }
        List<String> conditions = AnnotationUtil.switchFilterConditionToStr(edit.filter());
        conditions.add(dependCondition);
        EruptModel eruptReferenceModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
        if (!tabRef) {
            // 由于类加载顺序问题，并没有选择在启动时检测
            ReferenceTableType referenceTableType = eruptFieldModel.getEruptField().edit().referenceTableType();
            Erupts.requireTrue(
                    eruptReferenceModel.getEruptFieldMap().containsKey(referenceTableType.label().split("\\.")[0]),
                    eruptReferenceModel.getEruptName() + " not found '" + referenceTableType.label()
                            + "' field，please use @ReferenceTableType annotation 'label' config");
        }
        return eruptService.getEruptData(eruptReferenceModel, tableQueryVo, null, conditions.toArray(new String[0]));
    }

    @SneakyThrows
    @GetMapping("/depend-tree/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public Collection<TreeModel> getDependTree(@PathVariable("erupt") String erupt) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        String field = eruptModel.getErupt().linkTree().field();
        if (null == eruptModel.getEruptFieldMap().get(field)) {
            String treeErupt = eruptModel.getClazz().getDeclaredField(field).getType().getSimpleName();
            return this.getEruptTreeData(treeErupt);
        } else {
            return this.getReferenceTree(eruptModel.getEruptName(), field, null);
        }
    }

    @GetMapping("/{erupt}/reference-tree/{fieldName}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Collection<TreeModel> getReferenceTree(@PathVariable("erupt") String erupt,
            @PathVariable("fieldName") String fieldName,
            @RequestParam(value = "dependValue", required = false) Serializable dependValue) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        Erupts.powerLegal(eruptModel,
                powerObject -> powerObject.isEdit() || powerObject.isAdd()
                        || eruptFieldModel.getEruptField().edit().search().value()
                        || StringUtils.isNotBlank(eruptModel.getErupt().linkTree().field()));
        String dependField = eruptFieldModel.getEruptField().edit().referenceTreeType().dependField();
        if (!AnnotationConst.EMPTY_STR.equals(dependField)) {
            Erupts.requireNonNull(dependValue,
                    "请先选择" + eruptModel.getEruptFieldMap().get(dependField).getEruptField().edit().title());
        }
        Edit edit = eruptFieldModel.getEruptField().edit();
        ReferenceTreeType treeType = edit.referenceTreeType();
        EruptModel referenceEruptModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
        Erupts.requireTrue(referenceEruptModel.getEruptFieldMap().containsKey(treeType.label().split("\\.")[0]),
                referenceEruptModel.getEruptName() + " not found " + treeType.label()
                        + " field, please use @ReferenceTreeType annotation config");
        List<Condition> conditions = new ArrayList<>();
        // 处理depend参数代码
        if (StringUtils.isNotBlank(treeType.dependField()) && null != dependValue) {
            conditions.add(new Condition(edit.referenceTreeType().dependColumn(), dependValue));
        }
        List<String> conditionStrings = AnnotationUtil.switchFilterConditionToStr(edit.filter());
        return preEruptDataService.geneTree(referenceEruptModel, treeType.id(), treeType.label(), treeType.pid(),
                treeType.rootPid(), EruptQuery.builder().orderBy(edit.orderBy()).conditionStrings(conditionStrings)
                        .conditions(conditions).build());
    }

    @PostMapping("/validate-erupt/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel validateErupt(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.SUCCESS) {
            DataProxyInvoke.invoke(eruptModel,
                    (dataProxy -> dataProxy.beforeAdd(gson.fromJson(data.toString(), eruptModel.getClazz()))));
        }
        eruptApiModel.setErrorIntercept(false);
        eruptApiModel.setPromptWay(EruptApiModel.PromptWay.MESSAGE);
        return eruptApiModel;
    }

}