package xyz.erupt.annotation.fun;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2020-05-21
 */
@Getter
@Setter
public class VLModel {

    private String value;

    private String text;

    private String color;

    private String desc;

    private boolean disable;

    public VLModel(Long value, String label, String color,String desc, boolean disable) {
        this(value+"", label, color, desc, disable);
    }
    public VLModel(String value, String label, String color,String desc, boolean disable) {
        this.value = value;
        this.text = label;
        this.color = color;
        this.desc = desc;
        this.disable = disable;
    }

    public VLModel() {
    }
    public String getLabel() {return this.text;}
}
