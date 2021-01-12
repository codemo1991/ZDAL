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
package com.zdal.sharding.core.session.impl;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zdal.sharding.core.config.RouterHolderConfig;
import com.zdal.sharding.core.context.ZDalContext;
import com.zdal.sharding.core.router.table.AbstractRouter;
import com.zdal.sharding.core.session.IClusterSqlSession;
import com.zdal.sharding.core.session.factory.IClusterSqlSessionFactory;
import com.zdal.sharding.core.shard.IShardInfo;
import com.zdal.sharding.core.shard.impl.ShardInfoImpl;
import com.zdal.sharding.core.utils.StringUtil;

/**
 * Name: ClusterSqlSessionImpl.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.session.impl.ClusterSqlSessionImpl.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月14日 下午5:20:17
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
public class ClusterSqlSessionImpl implements IClusterSqlSession { 
	
	private static ThreadLocal<String> currentSubgraphShardId = new ThreadLocal<String>();

	private final IClusterSqlSessionFactory clusterSqlSessionFactory;

	private final List<IShardInfo> shards;

	private final Map<String, IShardInfo> shardNamesToShards;
	
	
	public ClusterSqlSessionImpl(IClusterSqlSessionFactory clusterSqlSessionFactory) {
		
		this.shards = buildShardListFromSqlSessionFactoryShardIdMap(clusterSqlSessionFactory.getSqlSessionFactoryShardNameMap());
		this.shardNamesToShards = buildShardIdsToShardsMap();
		this.clusterSqlSessionFactory = clusterSqlSessionFactory;
	}

	
	static List<IShardInfo> buildShardListFromSqlSessionFactoryShardIdMap(
			Map<SqlSessionFactory, Set<String>> sqlSessionFactoryShardIdMap
			 ) {
		List<IShardInfo> list = Lists.newArrayList();
		for (Map.Entry<SqlSessionFactory, Set<String>> entry : sqlSessionFactoryShardIdMap
				.entrySet()) {
			IShardInfo shard = new ShardInfoImpl(entry.getValue(), entry.getKey());
			list.add(shard);

		}

		return list;
	}
	
	private Map<String, IShardInfo> buildShardIdsToShardsMap() {
		Map<String, IShardInfo> map = Maps.newHashMap();
		for (IShardInfo shard : shards) {
				for( String shardName : shard.getShardNames() ){
					map.put(shardName, shard);
				}
		}
		return map;
	}
	
	private SqlSession getSqlSession( String statement , Object paramObj ){
		MappedStatement mstt = clusterSqlSessionFactory.getConfiguration().getMappedStatement(statement);
		BoundSql boundSql = mstt.getBoundSql(null);
		String sql = boundSql.getSql();
		SQLStatement stmt = SQLUtils.parseStatements(sql, ZDalContext.DBTYPE).get(0);
		if( stmt == null )
			throw new RuntimeException("with sql error");
		MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);
        String tableName = visitor.getCurrentTable();
        if(StringUtil.isBlank(tableName)){
            tableName = fetchFirstTableName(visitor.getTables());
        }
		AbstractRouter router = RouterHolderConfig.tableNameMapRouter.get(tableName);
		if( router == null  )
			return this.shardNamesToShards.get(ZDalContext.DEFUALTDATASOURCENAME).establishSqlSession();
		
		if( !this.shardNamesToShards.containsKey(router.getDbShardName(paramObj)) )
			throw new RuntimeException(String.format("The DbShard of %s with %s had not exit.",tableName,router.getDbShardName(paramObj)));
		return this.shardNamesToShards.get(router.getDbShardName(paramObj)).establishSqlSession();
	}
	
	/**
	 * 
	 * 创建时间：2017年4月18日 上午9:34:52
	 * 创建人: 林继丰
	 * 方法描述：连接查询无法获得currentTable,这里使用主表作为路由策略表
	 * @param tableNames
	 * @return
	 */
	private String fetchFirstTableName(Map<TableStat.Name,TableStat> tableNames){
	    if(tableNames.size() > 0)
	        return tableNames.entrySet().iterator().next().getKey().getName();
	    return null;
	}

	@Override
	public <T> T selectOne(String statement) {
		return this.getSqlSession(statement ,null).selectOne(statement);
	}

	@Override
	public <T> T selectOne(String statement, Object parameter) {
		return this.getSqlSession(statement,parameter).selectOne(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement) {
		return this.getSqlSession(statement,null).selectList(statement);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter) {
		return this.getSqlSession(statement,parameter).selectList(statement,parameter);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
		return this.getSqlSession(statement,parameter).selectList(statement,parameter,rowBounds);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		return this.getSqlSession(statement,null).selectMap(statement, null, mapKey);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		return this.getSqlSession(statement,parameter).selectMap(statement, parameter, mapKey, RowBounds.DEFAULT);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		return this.getSqlSession(statement,parameter).selectMap(statement, parameter, mapKey, rowBounds);

	}

	@Override
	public <T> Cursor<T> selectCursor(String statement) {
		return this.getSqlSession(statement,null).selectCursor(statement);
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement, Object parameter) {
		return this.getSqlSession(statement,parameter).selectCursor(statement,parameter);
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
		return this.getSqlSession(statement,parameter).selectCursor(statement,parameter,rowBounds);
	}

	@Override
	public void select(String statement, ResultHandler handler) {
		throw new UnsupportedOperationException(
				"opration select is not allowed over a ShardedSqlSession");
	}

	@Override
	public void select(String statement, Object parameter, ResultHandler handler) {
		throw new UnsupportedOperationException(
				"opration select is not allowed over a ShardedSqlSession");
	}

	@Override
	public void select(String statement, Object parameter, RowBounds rowBounds,
			ResultHandler handler) {
		throw new UnsupportedOperationException(
				"opration select is not allowed over a ShardedSqlSession");
	}

	@Override
	public int insert(String statement) {
		return this.getSqlSession(statement,null).insert(statement);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return this.getSqlSession(statement,parameter).insert(statement, parameter);
	}

	@Override
	public int update(String statement) {
		return this.getSqlSession(statement,null).update(statement);
	}

	@Override
	public int update(String statement, Object parameter) {
		return this.getSqlSession(statement,parameter).update(statement, parameter);
	}

	@Override
	public int delete(String statement) {
		return this.getSqlSession(statement,null).delete(statement);
	}

	@Override
	public int delete(String statement, Object parameter) {
		return this.getSqlSession(statement,parameter).delete(statement,parameter);
	}

	@Override
	public void commit() {
		commit(false);
	}

	@Override
	public void commit(boolean force) {
		
	}

	@Override
	public void rollback() {
		rollback(false);
	}

	@Override
	public void rollback(boolean force) {

	}

	@Override
	public List<BatchResult> flushStatements() {
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public void clearCache() {
		for( IShardInfo shardInfo : this.shards ){
			SqlSession session = shardInfo.establishSqlSession();
			if( session != null )
				session.clearCache();
		}
	}

	@Override
	public Configuration getConfiguration() {
		throw new UnsupportedOperationException(
				"Manual get configuration is not allowed over a Spring managed SqlSession");
	}

	@Override
	public <T> T getMapper(Class<T> type) {
		for( IShardInfo shardInfo : this.shards ){
			SqlSession session = shardInfo.establishSqlSession();
			if( session != null )
				return session.getMapper(type);
		}
		
		throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
	}

	@Override
	public Connection getConnection() {
		throw new UnsupportedOperationException(
				"Manual get connection is not allowed over a Spring managed SqlSession");
	}

	
	
	private IShardInfo getShardByStatement(String statement, List<IShardInfo> shardsToConsider) {
		for (IShardInfo shard : shardsToConsider) {
			if (shard.getSqlSessionFactory() != null
					&& shard.getMappedStatementNames().contains(statement)) {
				return shard;
			}
		}
		return null;
	}
	
	@Override
	public SqlSession getSqlSessionForStatement(String statement) {
		IShardInfo info = getShardByStatement(statement, this.shards);
		if( info == null )
			return null;
		return info.establishSqlSession();
	}

}
