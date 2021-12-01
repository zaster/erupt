package xyz.erupt.annotation.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.annotation.config.QueryExpression;

@Getter
@Setter
@NoArgsConstructor
public class Condition {

    private String key;

    private Object value;

    private transient QueryExpression expression = QueryExpression.EQ;

    public Condition(String key, Object value, QueryExpression expression) {
        this.key = key;
        this.value = value;
        this.expression = expression;
    }

    public Condition(String key, Object value) {
        this.key = key;
        this.value = value;
    }

}
