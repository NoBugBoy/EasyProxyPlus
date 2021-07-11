package utils;

import java.util.List;
import java.util.Map;

/**
 * author yujian
 * description
 * create 2021-03-05 13:02
 **/
public class YamlEntity {
    private List<Map<String, Object>> proxys;

    public List<Map<String, Object>> getProxys() {
        return proxys;
    }

    public void setProxys(List<Map<String, Object>> proxys) {
        this.proxys = proxys;
    }

    @Override
    public String toString() {
        return "YamlEntity{" + "proxys=" + proxys + '}';
    }
}
