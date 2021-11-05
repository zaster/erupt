package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.CloneSupport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2018-09-29.
 */
@Getter
@Setter
public final class EruptModel implements Cloneable {

    private transient Class<?> clazz;

    private transient Erupt erupt;

    private transient Map<String, EruptFieldModel> eruptFieldMap;

    private String eruptName;

    private JsonObject eruptJson;

    private List<EruptFieldModel> eruptFieldModels;

    public EruptModel(Class<?> eruptClazz) {
        this.clazz = eruptClazz;
        this.erupt = eruptClazz.getAnnotation(Erupt.class);
        this.eruptName = eruptClazz.getSimpleName();
        this.eruptJson = AnnotationUtil.annotationToJsonByReflect(this.erupt);
    }

    @Override
    public final EruptModel clone() throws CloneNotSupportedException {
        EruptModel eruptModel = (EruptModel) super.clone();
        eruptModel.eruptFieldModels = eruptFieldModels.stream().map(CloneSupport::clone).collect(Collectors.toList());
        return eruptModel;
    }

}
