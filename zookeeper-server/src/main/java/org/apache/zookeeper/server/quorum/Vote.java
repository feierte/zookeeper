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

package org.apache.zookeeper.server.quorum;

import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;


/**
 * 投票规则：
 *  1、首先对比 zxid。zxid 大的服务器优先作为 Leader
 *  2、若 zxid 相同，比如初始化的时候，每个 Server 的 zxid 都为0，就会比较 myid，myid 大的选出来做 Leader。
 */
public class Vote {
    
    public Vote(long id,
                    long zxid) {
        this.version = 0x0;
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = -1;
        this.peerEpoch = -1;
        this.state = ServerState.LOOKING;
    }
    
    public Vote(long id,
                    long zxid,
                    long peerEpoch) {
        this.version = 0x0;
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = -1;
        this.peerEpoch = peerEpoch;
        this.state = ServerState.LOOKING;
    }

    public Vote(long id,
                    long zxid,
                    long electionEpoch,
                    long peerEpoch) {
        this.version = 0x0;
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = electionEpoch;
        this.peerEpoch = peerEpoch;
        this.state = ServerState.LOOKING;
    }
    
    public Vote(int version,
                    long id,
                    long zxid,
                    long electionEpoch,
                    long peerEpoch,
                    ServerState state) {
        this.version = version;
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = electionEpoch;
        this.state = state;
        this.peerEpoch = peerEpoch;
    }
    
    public Vote(long id,
                    long zxid,
                    long electionEpoch,
                    long peerEpoch,
                    ServerState state) {
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = electionEpoch;
        this.state = state;
        this.peerEpoch = peerEpoch;
        this.version = 0x0;
    }

    final private int version;

    /**
     * 服务器编号 id。在 zoo.cfg 配置文件中配置 myid。
     * 比如有三台服务器，编号分别是1，2，3。编号越大在选举算法中的权重越大。
     */
    final private long id;

    /**
     * 被推举的Leader事务ID，服务器中存放的最新数据version，值越大说明数据越新，在选举中数据越新权重越大。
     */
    final private long zxid;

    /**
     * 逻辑时钟。
     * 用来判断多个投票是否在同一轮选举周期中，该值在服务端是一个自增序列，每次进入新一轮的投票后，都会对该值进行加1操作。
     */
    final private long electionEpoch;
    
    final private long peerEpoch;
    
    public int getVersion() {
        return version;
    }

    public long getId() {
        return id;
    }

    public long getZxid() {
        return zxid;
    }

    public long getElectionEpoch() {
        return electionEpoch;
    }

    public long getPeerEpoch() {
        return peerEpoch;
    }

    public ServerState getState() {
        return state;
    }

    final private ServerState state;
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vote)) {
            return false;
        }
        Vote other = (Vote) o;

        if ((state == ServerState.LOOKING) ||
                (other.state == ServerState.LOOKING)) {
            return (id == other.id
                    && zxid == other.zxid
                    && electionEpoch == other.electionEpoch
                    && peerEpoch == other.peerEpoch);
        } else {
            /*
             * There are two things going on in the logic below:
             * 
             * 1. skip comparing the zxid and electionEpoch for votes for servers 
             *    out of election. 
             *    
             *    Need to skip those because they can be inconsistent due to  
             *    scenarios described in QuorumPeer.updateElectionVote. 
             *
             *    And given that only one ensemble can be running at a single point 
             *    in time and that each epoch is used only once, using only id and 
             *    epoch to compare the votes is sufficient.
             *
             *    {@see https://issues.apache.org/jira/browse/ZOOKEEPER-1805}
             *
             * 2. skip comparing peerEpoch if if we're running with mixed ensemble 
             *    with (version > 0x0) and without the change (version = 0x0) 
             *    introduced in ZOOKEEPER-1732.
             *
             *    {@see https://issues.apache.org/jira/browse/ZOOKEEPER-1732}
             *
             *    The server running with and without ZOOKEEPER-1732 will return 
             *    different peerEpoch. During rolling upgrades, it's possible
             *    that 2/5 servers are returning epoch 1, while the other 2/5
             *    are returning epoch 2, the other server need to ignore the 
             *    peerEpoch to be able to join it.
             */
            if ((version > 0x0) ^ (other.version > 0x0)) {
                return id == other.id;
            } else {
                return (id == other.id
                        && peerEpoch == other.peerEpoch);
            }
        }
    }

    @Override
    public int hashCode() {
        return (int) (id & zxid);
    }

    public String toString() {
        return "(" + id + ", " + Long.toHexString(zxid) + ", " + Long.toHexString(peerEpoch) + ")";
    }
}
