package xyz.erupt.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import xyz.erupt.core.view.TreeModel;

/**
 * @author YuePeng
 * date 2019-04-28.
 */
@Slf4j
public class DataHandlerUtil {

    // 引用方式 生成树结构数据
    public static List<TreeModel> quoteTree(List<TreeModel> treeModels) {
        Map<String, TreeModel> treeModelMap = new LinkedHashMap<>(treeModels.size());
        treeModels.forEach(treeModel -> treeModelMap.put(treeModel.getId(), treeModel));
        List<TreeModel> resultTreeModels = new ArrayList<>();
        treeModels.forEach(treeModel -> {
            if (treeModel.isRoot()) {
                treeModel.setLevel(1);
                resultTreeModels.add(treeModel);
                return;
            }
            Optional.ofNullable(treeModelMap.get(treeModel.getPid())).ifPresent(parentTreeModel -> {
                Collection<TreeModel> children = CollectionUtils.isEmpty(parentTreeModel.getChildren()) ? new ArrayList<>() : parentTreeModel.getChildren();
                children.add(treeModel);
                children.forEach(child -> child.setLevel(Optional.ofNullable(parentTreeModel.getLevel()).orElse(1) + 1));
                parentTreeModel.setChildren(children);
            });
        });
        return resultTreeModels;
    }
}
  