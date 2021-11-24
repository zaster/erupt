package xyz.erupt.upms.handler;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.util.EruptAssert;
import xyz.erupt.upms.cache.CaffeineEruptCache;
import xyz.erupt.upms.constant.FetchConst;

/**
 * @author YuePeng
 * date 2021/01/03 18:00
 */
@Component
public class SqlChoiceFetchHandler implements ChoiceFetchHandler {

    private final CaffeineEruptCache<List<VLModel>> sqlCache = new CaffeineEruptCache<>();

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<VLModel> fetch(String[] params) {
        EruptAssert.notNull(params,SqlChoiceFetchHandler.class.getSimpleName() + " → params not found");
        sqlCache.init(params.length == 2 ? Long.parseLong(params[1]) : FetchConst.DEFAULT_CACHE_TIME);
        return sqlCache.get(SqlChoiceFetchHandler.class.getName() + ":" + params[0], (key) -> jdbcTemplate.query(params[0], (rs, i) -> {
            if (rs.getMetaData().getColumnCount() == 1) {
                return new VLModel(rs.getString(1), rs.getString(1),"","",false);
            } else {
                return new VLModel(rs.getString(1), rs.getString(2),"","",false);
            }
        }));
    }

}
