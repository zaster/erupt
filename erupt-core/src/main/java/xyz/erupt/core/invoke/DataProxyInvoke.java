package xyz.erupt.core.invoke;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 2021/3/16 13:37
 */
public class DataProxyInvoke {

    public static <T> void invoke(EruptModel eruptModel, Consumer<DataProxy<T>> consumer) {
        //父类及接口 @PreDataProxy
        ReflectUtil.findClassExtendStack(eruptModel.getClazz()).forEach(clazz -> DataProxyInvoke.actionInvokePreDataProxy(clazz, consumer));
        //本类及接口 @PreDataProxy
        DataProxyInvoke.actionInvokePreDataProxy(eruptModel.getClazz(), consumer);
        //@Erupt → DataProxy
        Stream.of(eruptModel.getErupt().dataProxy()).forEach(proxy -> consumer.accept(getInstanceBean(proxy)));
    }

    private static <T> void actionInvokePreDataProxy(Class<?> clazz, Consumer<DataProxy<T>> consumer) {
        //接口
        Stream.of(clazz.getInterfaces()).forEach(it -> Optional.ofNullable(it.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> consumer.accept(getInstanceBean(dataProxy.value()))));
        //类
        Optional.ofNullable(clazz.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> consumer.accept(getInstanceBean(dataProxy.value())));
    }

    private static <T>DataProxy<T> getInstanceBean(Class<? extends DataProxy<?>> dataProxy) {
        return (DataProxy<T>) EruptSpringUtil.getBean(dataProxy);
    }

}
