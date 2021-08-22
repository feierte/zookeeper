package org.apache.zookeeper.demo.config;

/**
 * 配置信息序列化，将配置信息转换成字节数组
 *
 * @author Jie Zhao
 * @date 2021/8/22 14:39
 */
public interface ConfigWriter<T> {

    /**
     * 根据配置信息的形式，将配置信息序列化
     * @param config 配置信息的形式，例如Properties形式的配置信息、YAML形式的配置信息等等，
     * @return
     */
    void writer(T config);
}
