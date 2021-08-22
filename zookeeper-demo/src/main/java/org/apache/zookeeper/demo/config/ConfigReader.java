package org.apache.zookeeper.demo.config;

/**
 * 配置文件读取器
 *
 * @author Jie Zhao
 * @date 2021/8/22 14:45
 */
public interface ConfigReader<T> {


    /**
     * 加载一个配置项
     *
     * @param configItemName
     * @return
     */
    String loadConfigItem(String configItemName);

    /**
     * 加载整个配置文件
     *
     * @param configName 配置文件名称
     * @return
     */
    T loadConfig(String configName);

    /**
     * @param data
     * @return
     */
    T read(byte[] data);

}
