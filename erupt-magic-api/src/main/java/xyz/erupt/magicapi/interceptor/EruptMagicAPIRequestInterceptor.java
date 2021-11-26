package xyz.erupt.magicapi.interceptor;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.Options;
import org.ssssssss.script.MagicScriptContext;

import xyz.erupt.magicapi.service.MagicAPIDataLoadService;
import xyz.erupt.upms.cache.CaffeineEruptCache;
import xyz.erupt.upms.cache.IEruptCache;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

/**
 * magic-api UI鉴权、接口鉴权
 */
@Component
public class EruptMagicAPIRequestInterceptor implements RequestInterceptor, AuthorizationInterceptor {

    private final EruptUserService eruptUserService;

    private final EruptContextService eruptContextService;

    public EruptMagicAPIRequestInterceptor(EruptUserService eruptUserService, EruptContextService eruptContextService) {
        this.eruptUserService = eruptUserService;
        this.eruptContextService = eruptContextService;
    }

    /**
     * 配置UI界面不需要登录框
     */
    @Override
    public boolean requireLogin() {
        return false;
    }


    private final IEruptCache<EruptUser> eruptUserIEruptCache = new CaffeineEruptCache<>(1000 * 60 * 10);

    /**
     * 配置接口权限
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Override
    public Object preHandle(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) throws JsonMappingException, JsonProcessingException {
        String permission = Objects.toString(info.getOptionValue(Options.PERMISSION), "");
        String role = Objects.toString(info.getOptionValue(Options.ROLE), "");
        String login = Objects.toString(info.getOptionValue(Options.REQUIRE_LOGIN), "");
        boolean isLogin = eruptUserService.getCurrentUid() != null;
        if (StringUtils.isNotBlank(login) && !isLogin) {
            return new JsonBean<Void>(401, "用户未登录");
        }
        if (StringUtils.isNotBlank(role) || StringUtils.isNotBlank(permission)) {
            // 未登录
            if (!isLogin) {
                return new JsonBean<Void>(401, "用户未登录");
            } else {
                EruptUser user = eruptUserIEruptCache.get(eruptContextService.getCurrentToken(), key -> eruptUserService.getCurrentEruptUser());
                // 权限判断
                if (StringUtils.isNotBlank(permission) && eruptUserService.getEruptMenuByValue(permission) == null) {
                    return new JsonBean<Void>(403, "用户权限不足");
                }
                // 角色判断
                if (StringUtils.isNotBlank(role) && user.getRoles().stream().noneMatch(it -> role.equals(it.getCode()))) {
                    return new JsonBean<Void>(403, "用户权限不足");
                }
            }
        }
        return null;
    }

    /**
     * 配置UI鉴权
     */
    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization) {
        // 未登录或UI权限不足
        try {
            return eruptUserService.getCurrentUid() != null
                    && eruptUserService.getEruptMenuByValue(MagicAPIDataLoadService.MAGIC_API_MENU_PREFIX + authorization.name()) != null;
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}
