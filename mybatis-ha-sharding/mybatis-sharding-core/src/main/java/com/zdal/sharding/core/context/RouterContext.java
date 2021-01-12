/**
 * Copyright (C) 2016 mybatis-sharding-core Project
 *               Author: Administrator
 *               Date: 2016年6月12日
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
package com.zdal.sharding.core.context;

import java.util.HashMap;

import com.alibaba.druid.sql.ast.SQLStatement;

/**
 * Name: RouterContext.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.context.RouterContext.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年6月12日 下午3:05:36
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
public class RouterContext {
	
	public static final ThreadLocal<SQLStatement> localStatement = new ThreadLocal<SQLStatement>(); 
	
	public static SQLStatement getLocalStatement(){
		return localStatement.get();
	}
	
	public static void setLocalStatement( SQLStatement statement ){
		localStatement.set(statement);
	}

	public static void remove(){
		localStatement.remove();
	}
}
