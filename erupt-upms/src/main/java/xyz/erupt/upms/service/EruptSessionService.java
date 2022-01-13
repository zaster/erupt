package xyz.erupt.upms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.config.EruptUpmsConfig;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.enums.MenuStatus;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.vo.EruptMenuVo;

/**
 * @author YuePeng
 *         date 2019-08-13.
 */
@Component
public class EruptSessionService {
    @Resource
    private ObjectMapper mapper;

    @Resource
    private EruptProp eruptProp;
    @Resource
    private EruptUpmsConfig eruptUpmsConfig;
    @Resource
    private EruptDao eruptDao;
    @Resource
    private HttpServletRequest request;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private EruptContextService eruptContextService;

    // 获取当前菜单对象
    public EruptMenu getCurrentEruptMenu() throws JsonMappingException, JsonProcessingException {
        return this.getMapValue(SessionKey.MENU_VALUE_MAP + eruptContextService.getCurrentToken(),
                eruptContextService.getEruptHeader(),
                EruptMenu.class);
    }

    public Long getCurrentUid() {
        Object uid = get(SessionKey.USER_TOKEN + eruptContextService.getCurrentToken());
        return null == uid ? null : Long.valueOf(uid.toString());
    }

    // 获取当前登录用户对象
    public EruptUser getCurrentEruptUser() {
        Long uid = this.getCurrentUid();
        return null == uid ? null : eruptDao.getEntityManager().find(EruptUser.class, uid);
    }

    public void cacheUserInfo(EruptUser eruptUser, List<EruptMenu> eruptMenus, String token)
            throws JsonProcessingException {
        // List<EruptMenu> eruptMenus = eruptMenuService.getUserAllMenu(eruptUser);
        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> codeMap = new HashMap<>();
        for (EruptMenu menu : eruptMenus) {
            codeMap.put(menu.getCode(), menu);
            if (null != menu.getValue()) {
                valueMap.put(menu.getValue(), menu);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (EruptRole role : eruptUser.getRoles()) {
            sb.append(role.getPowerOff()).append("|");
        }
        this.putMap(SessionKey.MENU_VALUE_MAP + token, valueMap, eruptUpmsConfig.getExpireTimeByLogin());
        this.putMap(SessionKey.MENU_CODE_MAP + token, codeMap, eruptUpmsConfig.getExpireTimeByLogin());

        this.put(SessionKey.MENU_VIEW + token, mapper.writeValueAsString(this.geneMenuListVo(eruptMenus)),
                eruptUpmsConfig.getExpireTimeByLogin());
        this.put(SessionKey.ROLE_POWER + token, sb.toString(), eruptUpmsConfig.getExpireTimeByLogin());
    }

    private List<EruptMenuVo> geneMenuListVo(List<EruptMenu> menus) {
        List<EruptMenuVo> list = new ArrayList<>();
        menus.stream().filter(menu -> menu.getStatus() == MenuStatus.OPEN.getValue()).forEach(menu -> {
            Long pid = null == menu.getParentMenu() ? null : menu.getParentMenu().getId();
            list.add(new EruptMenuVo(menu.getId(), menu.getCode(), menu.getName(), menu.getType(), menu.getValue(),
                    menu.getIcon(), pid));
        });
        return list;
    }

    public void put(String key, String str, long timeout) {
        this.put(key, str, timeout, TimeUnit.MINUTES);
    }

    public void put(String key, String str, long timeout, TimeUnit timeUnit) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.opsForValue().set(key, str, timeout, timeUnit);
        } else {
            request.getSession().setAttribute(key, str);
        }
    }

    public void remove(String key) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.delete(key);
        } else {
            request.getSession().removeAttribute(key);
        }
    }

    public Object get(String key) {
        if (eruptProp.isRedisSession()) {
            return stringRedisTemplate.opsForValue().get(key);
        } else {
            return request.getSession().getAttribute(key);
        }
    }

    public <T> T get(String key, TypeReference<T> type) throws JsonMappingException, JsonProcessingException {
        if (eruptProp.isRedisSession()) {
            if (null == this.get(key)) {
                return null;
            } else {
                return mapper.readValue(this.get(key).toString(), type);
            }
        } else {
            return mapper.readValue(request.getSession().getAttribute(key).toString(), type);
        }
    }

    public void putMap(String key, Map<String, Object> map, long expire) {
        if (eruptProp.isRedisSession()) {
            BoundHashOperations<?, String, Object> boundHashOperations = stringRedisTemplate.boundHashOps(key);
            map.replaceAll((k, v) -> {
                try {
                    return mapper.writeValueAsString(v);
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            });
            boundHashOperations.putAll(map);
            boundHashOperations.expire(expire, TimeUnit.MINUTES);
        } else {
            request.getSession().setAttribute(key, map);
        }
    }

    public <T> T getMapValue(String key, String mapKey, Class<T> type)
            throws JsonMappingException, JsonProcessingException {
        if (eruptProp.isRedisSession()) {
            Object obj = stringRedisTemplate.boundHashOps(key).get(mapKey);
            if (null == obj) {
                return null;
            }
            return mapper.readValue(obj.toString(), type);
        } else {
            Map<String, T> map = (Map<String, T>) request.getSession().getAttribute(key);
            if (null == map) {
                return null;
            }
            return map.get(mapKey);
        }
    }

}
