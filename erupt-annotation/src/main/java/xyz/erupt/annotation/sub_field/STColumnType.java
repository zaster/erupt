package xyz.erupt.annotation.sub_field;


/**
 * @author YuePeng
 * date 2018-11-12.
 */
public enum STColumnType {
    
    TEXT(""),CHECKBOX , LINK , BADGE , TAG , RADIO , IMG , CURRENCY , NUMBER , DATE , YN , NO;
    private final String value;
    private STColumnType(){
        this.value = this.name();
    }
    private STColumnType(String type) {
        this.value = type;
    }
    public String getValue() {
		return value.toLowerCase();
	}
    @Override
    public String toString() {
        return value.toLowerCase();
    }
}
