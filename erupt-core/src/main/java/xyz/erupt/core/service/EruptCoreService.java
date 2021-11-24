package xyz.erupt.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.core.exception.EruptAnnotationException;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 9/28/18.
 */
@Order
@Service
@Slf4j
public class EruptCoreService implements ApplicationRunner {

    private static final Map<String, EruptModel<?>> ERUPTS = new LinkedCaseInsensitiveMap<>();

    public static  <TT>EruptModel<TT> getErupt(String eruptName) {
        return (EruptModel<TT>)ERUPTS.get(eruptName);
    }

    //需要动态构建的EruptModel视图属性
    @SneakyThrows
    public static <TT> EruptModel<TT> getEruptView(String eruptName) {
        EruptModel<TT> em = EruptCoreService.<TT>getErupt(eruptName).clone();
        for (EruptFieldModel fieldModel : em.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.type() == EditType.CHOICE) {
                fieldModel.setChoiceList(EruptUtil.getChoiceList(edit.choiceType()));
            } else if (edit.type() == EditType.TAGS) {
                fieldModel.setTagList(EruptUtil.getTagList(edit.tagsType()));
            }
            STColumn[] columns = fieldModel.getEruptField().columns();
            JSONArray jsonArray = new JSONArray();
            Arrays.stream(columns).forEach(column->{
                
                JSONObject jsonObject=AnnotationUtil.annotationToJsonByReflect(column);
                {
                    
                    String title = column.title();
                    String desc = column.desc();
                    JSONObject titleObject=new JSONObject();
                    titleObject.put("text", title);
                    titleObject.put("optionHelp", desc);
                    jsonObject.put("title", titleObject);
                }
                switch(column.type()) {
                    case BADGE:
                        
                        jsonObject.put("badge", new JSONObject(EruptUtil.getOptions(column.choices())));
                        break;
                    case YN:
                        jsonObject.put("type", "tag");
                        jsonObject.put("tag", new JSONObject(EruptUtil.getYn(column.bools())));
                        break;
                    case TAG:
                        jsonObject.put("tag", new JSONObject(EruptUtil.getOptions(column.choices())));
                        break;
                }
                
                jsonArray.add(jsonObject);
            });
            fieldModel.getEruptFieldJson().put("columns", jsonArray);
        }
        if (em.getErupt().rowOperation().length > 0) {
            boolean copy = false;
            for (RowOperation rowOperation : em.getErupt().rowOperation()) {
                if (!ExprInvoke.getExpr(rowOperation.show())) {
                    if (!copy) {
                        copy = true;
                        em.setEruptJson(em.getEruptJson().clone());
                    }
                    JSONArray jsonArray = em.getEruptJson().getJSONArray("rowOperation");
                    Optional.ofNullable(jsonArray).get().forEach(operation->{
                        if (rowOperation.code().equals(((JSONObject)operation).getString("code"))) {
                            jsonArray.remove(operation);
                            return;
                        }
                    });
                }
            }
        }
        return em;
    }

    private static <TT> EruptModel<TT> initEruptModel(Class<TT> clazz) {
        //erupt class data to memory
        EruptModel<TT> eruptModel = new EruptModel<TT>(clazz);
        // erupt field data to memory
        eruptModel.setEruptFieldModels(new ArrayList<>());
        eruptModel.setEruptFieldMap(new LinkedCaseInsensitiveMap<>());
        ReflectUtil.findClassAllFields(clazz, field -> Optional.ofNullable(field.getAnnotation(EruptField.class)).ifPresent(ignore -> {
            EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
            eruptModel.getEruptFieldModels().add(eruptFieldModel);
            eruptModel.getEruptFieldMap().put(field.getName(), eruptFieldModel);
        }));
        eruptModel.getEruptFieldModels().sort(Comparator.comparingInt((a) -> a.getEruptField().sort()));
        // erupt annotation validate
        EruptAnnotationException.validateEruptInfo(eruptModel);
        return eruptModel;
    }

    @Override
    public void run(ApplicationArguments args) {
        TimeRecorder timeRecorder = new TimeRecorder();
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(Erupt.class)
        }, clazz -> ERUPTS.put(clazz.getSimpleName(), initEruptModel(clazz)));
        log.info("Erupt core initialization completed in {} ms", timeRecorder.recorder());
    }
}
