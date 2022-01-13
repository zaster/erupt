package xyz.erupt.upms.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;

/**
 * @author YuePeng
 *         date 2021/8/20 00:41
 */
@Service
public class EruptContextService {

    @Resource
    private HttpServletRequest request;

    public String getEruptHeader() {
        String erupt = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
        if (StringUtils.isBlank(erupt)) {
            return request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
        }
        return erupt;
    }

    // 获取erupt上下文对象
    public Class<?> getContextEruptClass() {
        return EruptCoreService.getErupt(this.getEruptHeader()).getClazz();
    }

    // 获取当前请求token
    public String getCurrentToken() {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        if (StringUtils.isBlank(token)) {
            return request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
        }
        return token;
    }

}
