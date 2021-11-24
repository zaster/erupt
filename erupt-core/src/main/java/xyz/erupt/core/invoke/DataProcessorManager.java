package xyz.erupt.core.invoke;

import java.util.HashMap;
import java.util.Map;

import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author YuePeng
 * date 2020/12/4 16:24
 */
public class DataProcessorManager {

    private static final Map<String, Class<? extends IEruptDataService>> eruptDataServiceMap = new HashMap<>();

    public static void register(String name, Class<? extends IEruptDataService> eruptDataService) {
        eruptDataServiceMap.put(name, eruptDataService);
    }

    public static IEruptDataService getEruptDataProcessor(Class<?> clazz) {
        EruptDataProcessor eruptDataProcessor = clazz.getAnnotation(EruptDataProcessor.class);
        return EruptSpringUtil.getBean(eruptDataServiceMap.get(null == eruptDataProcessor ?
                EruptConst.DEFAULT_DATA_PROCESSOR : eruptDataProcessor.value()));
    }
}
