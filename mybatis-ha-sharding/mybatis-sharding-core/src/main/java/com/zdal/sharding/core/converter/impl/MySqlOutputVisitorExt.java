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
package com.zdal.sharding.core.converter.impl;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.zdal.sharding.core.config.RouterHolderConfig;
import com.zdal.sharding.core.router.table.AbstractRouter;

/**
 * Name: MySqlOutputVisitorExt.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.converter.impl.MySqlOutputVisitorExt.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年6月12日 上午11:59:30
 *
 * Update-User: @author
 * Update-Time:
 * Update-Remark:
 * 
 * Check-User:
 * Check-Time:
 * Check-Remark:
 * 
 * Company: kmy
 * Copyright: kmy
 */
public class MySqlOutputVisitorExt extends MySqlOutputVisitor {
	
	private final Object paramObj;

	/** 过滤子查询, 子查询不执行表名的替换 */
	private ThreadLocal<Boolean> HAS_SUBQUERY = new ThreadLocal<Boolean>();

	/** 
	 * @Title: MySqlOutputVisitorExt
	 * @Description: TODO
	 * @param @param appender
	 * @param @param router         
	 * @throws 
	 */ 
	public MySqlOutputVisitorExt(Appendable appender, Object paramObj) {
		super(appender);
		this.paramObj = paramObj;
	}
	
	@Override
	public boolean visit(SQLInSubQueryExpr x) {
		HAS_SUBQUERY.set(true);
		return super.visit(x);
	}
	
	@Override
	public void endVisit(SQLSelectStatement selectStatement) {
		HAS_SUBQUERY.set(false);
		super.endVisit(selectStatement);
	}
	
	@Override
	public boolean visit(MySqlInsertStatement x) {
		AbstractRouter router =  getRouterByTableName(x.getTableName().toString());
		if( router != null ){
			
			x.setTableSource(new SQLExprTableSource(new SQLIdentifierExpr(router.getInsertTableName(x.getTableName().toString(),paramObj))));
		}
		return super.visit(x);
	}

	@Override
	public boolean visit(MySqlDeleteStatement x) {
		AbstractRouter router =  getRouterByTableName(x.getTableName().toString());
		if( router != null ){
			x.setTableSource(new SQLExprTableSource(new SQLIdentifierExpr(router.getDeleteTableName(x.getTableName().toString(),paramObj))));
		}
		return super.visit(x);
		
	}

	@Override
	public boolean visit(MySqlSelectQueryBlock x) {
		if (HAS_SUBQUERY.get() != null && !HAS_SUBQUERY.get()) {
			return super.visit(x);	
		}
		setSelectTableName(x.getFrom());
		return super.visit(x);
	}

	@Override
	public boolean visit(MySqlUpdateStatement x) {
		AbstractRouter router = RouterHolderConfig.tableNameMapRouter.get(x.getTableName().toString());
		x.setTableSource(new SQLExprTableSource(new SQLIdentifierExpr(router.getUpdateTableName(x.getTableName().toString(),paramObj))));
		return super.visit(x);
	}
	
	
	private void setSelectTableName(SQLTableSource source) {
		if (source == null) return;
		if (source instanceof SQLJoinTableSource) {
			SQLJoinTableSource join = (SQLJoinTableSource) source;
			SQLTableSource left = join.getLeft();
			if (left != null && left instanceof SQLJoinTableSource) {
				
				setSelectTableName(join.getLeft());
			} else if (left != null && left instanceof SQLExprTableSource) {
				SQLExprTableSource expr = (SQLExprTableSource) left;
				if (expr.getExpr() instanceof SQLIdentifierExpr) {
					SQLIdentifierExpr identifier = (SQLIdentifierExpr) expr.getExpr();
					AbstractRouter router =  getRouterByTableName(identifier.getName());
					if( router != null ){
						final String targetTableName = router.getSelectTableName(identifier.getName(),paramObj);
						identifier.setName(targetTableName);
					}
				}
			}
			//不断转换连接的表
			setSelectTableName(join.getRight());
		} else if (source instanceof SQLExprTableSource) {
			SQLExprTableSource expr = (SQLExprTableSource) source;
			if (expr.getExpr() instanceof SQLIdentifierExpr) {
				SQLIdentifierExpr identifier = (SQLIdentifierExpr) expr.getExpr();
				AbstractRouter router =  getRouterByTableName(identifier.getName());
				if( router != null ){
					final String targetTableName = router.getSelectTableName(identifier.getName(),paramObj);
					identifier.setName(targetTableName);
				}
			}
		}
	}
	
	private AbstractRouter getRouterByTableName( String tableName ){
		AbstractRouter router = RouterHolderConfig.tableNameMapRouter.get(tableName);
		return router;
	}
	
	
}
