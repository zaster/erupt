package xyz.erupt.annotation.fun;

import java.util.List;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface ActionHandler<X,T,E> {

    /**
     * @param data        行数据
     * @param eruptObject 表单输入数据
     * @param param       注解回传参数
     * @return 事件触发成功后需要前端执行的 js 表达式
     */
    String exec(List<X> dependFromList,List<T> dependList,List<E> contentList,E contentForm, String[] param);

}
