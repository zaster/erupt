package xyz.erupt.core.invoke;

import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2021/3/16 13:37
 */
public class DataProxyInvoke {

    public static void invoke(EruptModel eruptModel, Consumer<DataProxy<Object>> consumer) {
        //父类及接口 @PreDataProxy
        ReflectUtil.findClassExtendStack(eruptModel.getClazz()).forEach(clazz -> DataProxyInvoke.actionInvokePreDataProxy(clazz, consumer));
        //本类及接口 @PreDataProxy
        DataProxyInvoke.actionInvokePreDataProxy(eruptModel.getClazz(), consumer);
        //@Erupt → DataProxy
        Stream.of(eruptModel.getErupt().dataProxy()).forEach(proxy -> consumer.accept(getInstanceBean(proxy)));
    }

    private static void actionInvokePreDataProxy(Class<?> clazz, Consumer<DataProxy<Object>> consumer) {
        //接口
        Stream.of(clazz.getInterfaces()).forEach(it -> Optional.ofNullable(it.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> consumer.accept(getInstanceBean(dataProxy.value()))));
        //类
        Optional.ofNullable(clazz.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> consumer.accept(getInstanceBean(dataProxy.value())));
    }

    private static DataProxy<Object> getInstanceBean(Class<? extends DataProxy<?>> dataProxy) {
        return (DataProxy<Object>) EruptSpringUtil.getBean(dataProxy);
    }

}
