/**
 * Copyright (C) 2016 mybatis-sharding-core Project
 *               Author: Administrator
 *               Date: 2016年7月18日
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zdal.sharding.core.config;

import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;

import com.zdal.sharding.core.shard.IShardInfo;

/**
 * Name: ClusterConfiguration.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.config.ClusterConfiguration.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月18日 上午11:21:52
 *
 * Update-User: @author
 * Update-Time:
 * Update-Remark:
 * 
 * Check-User:
 * Check-Time:
 * Check-Remark:
 * 
 * Company: 
 * Copyright: 
 */
public interface ClusterConfiguration {
	
	/**
	 * @return 此物理分区的唯一ID
	 */
	String getShardName();

	List<String> getShardInfos();

	/**
	 * @return 此物理分区所对应的数据源
	 */
	DataSource getShardDataSource();

	/**
	 * @see SqlSessionFactory
	 * @return 此物理分区的{@link SqlSessionFactory}
	 */
	SqlSessionFactory getSqlSessionFactory();


}
