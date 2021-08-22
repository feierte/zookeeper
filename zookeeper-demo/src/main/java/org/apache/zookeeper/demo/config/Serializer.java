package org.apache.zookeeper.demo.config;

/**
 * @author Jie Zhao
 * @date 2021/8/22 15:08
 */
public interface Serializer {

    /**
     * 根据配置信息的形式，将配置信息序列化
     * @param config 配置信息的形式，例如Properties形式的配置信息、YAML形式的配置信息等等，
     * @return
     */
    byte[] serialize(String config);
}
