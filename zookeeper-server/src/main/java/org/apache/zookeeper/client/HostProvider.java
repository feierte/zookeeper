/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.client;

import org.apache.yetus.audience.InterfaceAudience;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * A set of hosts a ZooKeeper client should connect to.
 * 
 * Classes implementing this interface must guarantee the following:
 * 
 * * Every call to next() returns an InetSocketAddress. So the iterator never
 * ends.
 * 
 * * The size() of a HostProvider may never be zero.
 * 
 * A HostProvider must return resolved InetSocketAddress instances on next() if the next address is resolvable.
 * In that case, it's up to the HostProvider, whether it returns the next resolvable address in the list or return
 * the next one as UnResolved.
 * 
 * Different HostProvider could be imagined:
 * 
 * * A HostProvider that loads the list of Hosts from an URL or from DNS 
 * * A HostProvider that re-resolves the InetSocketAddress after a timeout. 
 * * A HostProvider that prefers nearby hosts.
 *
 * @apiNote zookeeper 客户端需要连接的 zookeeper 服务端的地址集合。可通过轮询的方式获得服务端的所有地址。
 *
 * <p>StaticHostProvider 只是 ZooKeeper 官方提供的对于地址列表管理器的默认实现方式，也是最通用和最简单的一种实现方式。
 * 读者如果有需要的话，完全可以在满足 “HostProvider三要素” 的前提下，实现自己的服务器地址列表管理器。
 *  1、配置文件方式
 *      在 ZooKeeper 默认的实现方式中，是通过在构造方法中传入服务器地址列表的方式来实现地址列表的设置，但其实通常开发人员更习惯于将例如 IP 地址这样的配置信息保存在一个单独的配置文件中统一管理起来。
 *  针对这样的需求，我们可以自己实现一个 HostProvider，通过在应用启动的时候加载这个配置文件来实现对服务器地址列表的获取。
 *  2、动态变更的地址列表管理器
 *      在ZooKeeper的使用过程中，我们会碰到这样的问题：ZooKeeper服务器集群的整体迁移或个别机器的变更，会导致大批客户端应用也跟着一起进行变更。
 *  出现这个尴尬局面的本质原因是因为我们将一些可能会动态变更的 IP 地址写死在程序中了。因此，实现动态变更的地址列表管理器，对于提升 ZooKeeper 客户端用户使用体验非常重要。
 *      为了解决这个问题，最简单的一种方式就是实现这样一个 HostProvider：地址列表管理器能够定时从 DNS 或一个配置管理中心上解析出 ZooKeeper 服务器地址列表，
 *  如果这个地址列表变更了，那么就同时更新到 serverAddresses 集合中去，这样在下次需要获取服务器地址（即调用next()方法）的时候，就自然而然使用了新的服务器地址，随着时间推移，慢慢的就能够在保证客户端透明的情况下实现 ZooKeeper 服务器机器的变更。
 *  3、实现同机房优先策略
 *      随着业务增长，系统规模不断扩大，我们对于服务器机房的需求也日益旺盛。同时，随着系统稳定性和系统容灾等问题越来越被重视，很多互联网公司会出现多个机房，甚至是异地机房。
 *  多机房，在提高系统稳定性和容灾能力的同时，也给我们带来了一个新的困扰：如何解决不同机房之间的延时。
 *      所以在目前大规模的分布式系统设计中，我们开始考虑引入“同机房优先”的策略。所谓的“同机房优先”是指服务的消费者优先消费同一个机房中提供的服务。
 *  举个例子来说，一个服务F在杭州机房和北京机房中都有部署，那么对于杭州机房中的服务消费者，会优先调用杭州机房中的服务，对于北京机房的客户端也一样。
 *      对于 ZooKeeper 集群来说，为了达到容灾要求，通常会将集群中的机器分开部署在多个机房中，因此同样面临上述网络延时问题。对于这种情况，
 *  就可以实现一个能够优先和同机房 ZooKeeper 服务器创建的 HostProvider。
 */
@InterfaceAudience.Public
public interface HostProvider {

    /**
     * zookeeper 服务端地址的个数。
     * 该方法不能返回 0，也就是说，HostProvider 中必须至少有一个服务端地址。
     * @return
     */
    public int size();

    /**
     * The next host to try to connect to.
     * 
     * For a spinDelay of 0 there should be no wait.
     * 
     * @param spinDelay
     *            Milliseconds to wait if all hosts have been tried once.
     *
     * @apiNote 返回已经被解析的 InetSocketAddress 对象。该对象必须合法，也就是说不能返回null或其他不合法的InetSocketAddress。
     */
    public InetSocketAddress next(long spinDelay);

    /**
     * Notify the HostProvider of a successful connection.
     * 
     * The HostProvider may use this notification to reset it's inner state.
     */
    public void onConnected();

    /**
     * Update the list of servers. This returns true if changing connections is necessary for load-balancing, false otherwise.
     * @param serverAddresses new host list
     * @param currentHost the host to which this client is currently connected
     * @return true if changing connections is necessary for load-balancing, false otherwise  
     */
    boolean updateServerList(Collection<InetSocketAddress> serverAddresses,
        InetSocketAddress currentHost);
}
