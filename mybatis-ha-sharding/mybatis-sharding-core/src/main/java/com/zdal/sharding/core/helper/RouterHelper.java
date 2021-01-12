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
package com.zdal.sharding.core.helper;


import java.util.HashMap;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.zdal.sharding.core.config.RouterHolderConfig;
import com.zdal.sharding.core.context.RouterContext;
import com.zdal.sharding.core.router.table.AbstractRouter;
import com.zdal.sharding.core.utils.StringUtil;

/**
 * Name: RouterHelper.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.helper.RouterHelper.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年6月12日 下午3:08:13
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
public class RouterHelper {
	
	public static void setRouterInThread( String sql ){
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement statement = parser.parseStatement();
        
        
        if( StringUtil.isEmpty(sql) || statement == null )
        	throw new RuntimeException("error sql");
        
        RouterContext.setLocalStatement(statement);
	}
		
	
	/*private static String SelectQuery(SQLSelectQuery selectQuery) { 
		if (selectQuery instanceof SQLSelectQueryBlock) { 
			return ((SQLSelectQueryBlock) selectQuery).getFrom().toString(); 
		} else if (selectQuery instanceof SQLUnionQuery) { 
			return SelectQuery(((SQLUnionQuery) selectQuery).getLeft()); 
		} 
		return ""; 
	} */

}
