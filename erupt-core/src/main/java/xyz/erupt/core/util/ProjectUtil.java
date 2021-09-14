package xyz.erupt.core.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import xyz.erupt.core.constant.EruptConst;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2019-05-21.
 */
@Slf4j
public class ProjectUtil {

    private static final String LOADED_EXT = ".loaded";

    /**
     * @param projectName 标识名
     * @param first       bool回调，表示函数是否为第一次调用
     */
    @SneakyThrows
    public void projectStartLoaded(String projectName, Consumer<Boolean> first) {
        String userDir = System.getProperty("user.dir");
        File dirFile = new File(userDir, EruptConst.ERUPT_DIR);
        String warnTxt = " The erupt initialization ID file could not be created";
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            log.warn(dirFile + warnTxt);
        }
        File file = new File(dirFile.getPath(), projectName + LOADED_EXT);
        if (file.exists()) {
            first.accept(false);
        } else {
            first.accept(true);
            if (file.createNewFile()) {
                FileUtils.writeStringToFile(file, EruptPropUtil.getEruptVersion(), StandardCharsets.UTF_8);
            } else {
                log.warn(dirFile + warnTxt);
            }
        }
    }

}
