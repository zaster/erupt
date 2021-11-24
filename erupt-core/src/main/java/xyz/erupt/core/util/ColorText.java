package xyz.erupt.core.util;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ColorText {
    private String text;
    private String color;
    public ColorText(String text,String color){
        this.text = text;
        this.color = StringUtils.isBlank(color)?"default":color;
    }
}
