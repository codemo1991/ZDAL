/**
 * Copyright (C) 2016 mybatis-sharding-core Project
 *               Author: Administrator
 *               Date: 2016年7月14日
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
package com.zdal.sharding.core.session.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.session.SqlSessionFactory;
import com.zdal.sharding.core.session.IClusterSqlSession;

/**
 * Name: ClusterSqlSessionFactory.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.session.ClusterSqlSessionFactory.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月14日 下午5:17:05
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
public interface IClusterSqlSessionFactory extends SqlSessionFactory {
	
	
	List<SqlSessionFactory> getSqlSessionFactories();
	
	

	Map<SqlSessionFactory, Set<String>> getSqlSessionFactoryShardNameMap();

}
