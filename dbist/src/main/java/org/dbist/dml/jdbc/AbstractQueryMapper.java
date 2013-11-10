/**
 * Copyright 2011-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dbist.dml.jdbc;

import java.util.HashSet;
import java.util.Set;

import org.dbist.dml.Lock;
import org.dbist.metadata.Sequence;

/**
 * @author Steve M. Jung
 * @since 2013. 9. 7. (version 2.0.3)
 */
public abstract class AbstractQueryMapper implements QueryMapper {
	public String toNextval(Sequence sequence) {
		if (sequence.getName() == null || sequence.isAutoIncrement())
			return null;
		return sequence.getDomain() + "." + sequence.getName() + ".nextval";
	}

	public String toEscapement(char escape) {
		return "escape '" + escape + "'";
	}

	public String toWithLock(Lock lock) {
		return null;
	}

	public String toForUpdate(Lock lock) {
		StringBuffer buf = new StringBuffer();
		buf.append("for update");

		if (!isSupportedLockTimeout())
			return buf.toString();

		Integer timeout = lock.getTimeout();
		if (timeout == null || timeout < 0)
			return buf.toString();

		timeout /= 1000;
		if (timeout == 0)
			buf.append(" nowait");
		else
			buf.append(" wait " + timeout);
		return buf.toString();
	}

	private Set<String> reservedWords;
	public Set<String> getReservedWords() {
		if (reservedWords == null) {
			synchronized (this) {
				if (reservedWords == null) {
					Set<String> set = new HashSet<String>();

					set.add("absolute");

					// oracle
					set.add("access");
					// mysql
					set.add("accessible");
					// oracle
					set.add("account");

					set.add("action");

					set.add("add");
					// oracle
					set.add("admin");
					// oracle
					set.add("advice");

					set.add("after");

					set.add("all");
					// oracle
					set.add("all_rows");

					set.add("allocate");

					set.add("alter");
					// mysql, oracle
					set.add("analyze");

					set.add("and");

					set.add("any");
					// oracle
					set.add("archive");
					// oracle
					set.add("archivelog");

					set.add("are");

					set.add("array");

					set.add("as");

					set.add("asc");

					set.add("asensitive");

					set.add("assertion");

					set.add("asymmetric");
					// oracle
					set.add("at");

					set.add("atomic");
					// oracle
					set.add("audit");
					// oracle
					set.add("authenticated");

					set.add("authorization");
					// oracle
					set.add("autoextend");
					// oracle
					set.add("automatic");

					set.add("avg");
					// oracle
					set.add("backup");
					// oracle
					set.add("become");

					set.add("before");

					set.add("begin");

					set.add("between");
					// oracle
					set.add("bfile");

					set.add("bigint");

					set.add("binary");

					set.add("bit");
					// oracle
					set.add("bitmap");

					set.add("bitlength");

					set.add("blob");
					// oracle
					set.add("block");
					// oracle
					set.add("body");

					set.add("boolean");

					set.add("both");

					set.add("breath");

					set.add("by");

					set.add("call");
					// oracle
					set.add("cache");
					// oracle
					set.add("cache_instances");
					// mysql
					set.add("call");
					// oracle
					set.add("cancel");

					set.add("cascade");

					set.add("cascaded");

					set.add("case");

					set.add("cast");

					set.add("catalog");
					// oracle
					set.add("cfile");
					// oracle
					set.add("chained");
					// mysql, oracle
					set.add("change");

					set.add("char");
					// oracle
					set.add("char_cs");

					set.add("char_length");

					set.add("character");

					set.add("character_length");

					set.add("check");
					// oracle
					set.add("checkpoint");
					// oracle
					set.add("choose");
					// oracle
					set.add("chunk");
					// oracle
					set.add("clear");

					set.add("clob");
					// oracle
					set.add("clone");

					set.add("close");
					// oracle
					set.add("close_cached_open_cursors");
					// oracle
					set.add("cluster");

					set.add("coalesce");

					set.add("collate");

					set.add("collation");

					set.add("column");
					// oracle
					set.add("columns");
					// oracle
					set.add("comment");

					set.add("commit");
					// oracle
					set.add("commited");
					// oracle
					set.add("compatibility");
					// oracle
					set.add("compile");
					// oracle
					set.add("complete");
					// oracle
					set.add("composit_limit");
					// oracle
					set.add("compress");
					// oracle
					set.add("compute");

					set.add("condition");

					set.add("connect");

					set.add("connection");
					// oracle
					set.add("connect_time");

					set.add("constraint");

					set.add("constraints");

					set.add("constructure");

					set.add("contains");
					// oracle
					set.add("contents");

					set.add("continue");
					// oracle
					set.add("controlfile");

					set.add("convert");

					set.add("corresponding");
					// oracle
					set.add("cost");

					set.add("count");
					// oracle
					set.add("cpu_per_call");
					// oracle
					set.add("cpu_per_session");

					set.add("create");

					set.add("cross");

					set.add("cube");
					// oracle
					set.add("current");
					// oracle
					set.add("current_schema");

					set.add("current_date");

					set.add("current_default_transform_group");

					set.add("current_path");

					set.add("current_role");

					set.add("current_time");

					set.add("current_timestamp");

					set.add("current_transform_group_for_type");

					set.add("current_user");

					set.add("cursor");

					set.add("cycle");
					// oracle
					set.add("dangling");

					set.add("data");
					// mysql, oracle
					set.add("database");
					// mysql
					set.add("databases");
					// oracle
					set.add("datafile");
					// oracle
					set.add("dataobjno");

					set.add("date");

					set.add("day");
					// mysql
					set.add("day_hour");
					// mysql
					set.add("day_microsecond");
					// mysql
					set.add("day_minute");
					// mysql
					set.add("day_second");
					// oracle
					set.add("dba");
					// oracle
					set.add("dbhigh");
					// oracle
					set.add("dblow");
					// oracle
					set.add("dbmac");

					set.add("deallocate");
					// oracle
					set.add("debug");

					set.add("dec");

					set.add("decimal");

					set.add("declare");

					set.add("default");

					set.add("deferrable");

					set.add("deferred");
					// oracle
					set.add("degree");
					// mysql
					set.add("delayed");

					set.add("delete");

					set.add("depth");

					set.add("deref");

					set.add("desc");

					set.add("describe");

					set.add("descriptor");

					set.add("deterministic");

					set.add("diagnostics");

					set.add("disconnect");

					// oracle
					set.add("directory");
					// oracle
					set.add("disable");
					// oracle
					set.add("disconnect");
					// oracle
					set.add("dismount");

					set.add("distinct");
					// mysql
					set.add("distinctrow");
					// oracle
					set.add("distributed");
					// mysql
					set.add("div");
					// oracle
					set.add("dml");

					set.add("do");

					set.add("domain");

					set.add("double");

					set.add("drop");

					set.add("dynamic");
					// mysql
					set.add("dual");
					// oracle
					set.add("dump");

					set.add("each");

					set.add("else");

					set.add("elseif");
					// oracle
					set.add("enable");
					// mysql
					set.add("enclosed");

					set.add("end");

					set.add("equals");
					// oracle
					set.add("enforce");
					// oracle
					set.add("entry");

					set.add("escape");
					// mysql
					set.add("escaped");

					set.add("except");

					set.add("exception");
					// oracle
					set.add("exceptions");
					// oracle
					set.add("exchange");
					// oracle
					set.add("excluding");
					// oracle
					set.add("exclusive");

					set.add("exec");

					set.add("execute");

					set.add("exists");

					set.add("exit");

					set.add("external");

					set.add("extract");
					// oracle
					set.add("expire");
					// mysql
					set.add("explain");
					// oracle
					set.add("extent");
					// oracle
					set.add("extents");
					// oracle
					set.add("externally");
					// oracle
					set.add("failed_login_attempts");

					set.add("false");
					// oracle
					set.add("fast");

					set.add("fetch");
					// oracle
					set.add("file");

					set.add("filter");

					set.add("first");
					// oracle
					set.add("first_rows");
					// oracle
					set.add("flagger");

					set.add("float");
					// mysql
					set.add("float4");
					// mysql
					set.add("float8");
					// oracle
					set.add("flob");
					// oracle
					set.add("flush");

					set.add("for");
					// mysql, oracle
					set.add("force");

					set.add("foreign");

					set.add("found");

					set.add("free");
					// oracle
					set.add("freelist");
					// oracle
					set.add("freelists");

					set.add("from");

					set.add("full");
					// mysql
					set.add("fulltext");

					set.add("function");

					set.add("general");

					set.add("get");

					set.add("global");
					// oracle
					set.add("globallys");
					// oracle
					set.add("global_name");

					set.add("go");

					set.add("goto");

					set.add("grant");

					set.add("group");

					set.add("grouping");
					// oracle
					set.add("groups");

					set.add("handler");
					// oracle
					set.add("hash");
					// oracle
					set.add("hashkeys");

					set.add("having");
					// oracle
					set.add("header");
					// oracle
					set.add("heap");
					// mysql
					set.add("high_priority");

					set.add("hold");

					set.add("hour");
					// mysql
					set.add("hour_microsecond");
					// mysql
					set.add("hour_minute");
					// mysql
					set.add("hour_second");
					// oracle
					set.add("identified");

					set.add("identity");
					// oracle
					set.add("idgenerators");
					// oracle
					set.add("idle_time");

					set.add("if");
					// mysql
					set.add("ignore");

					set.add("immediate");

					set.add("in");
					// oracle
					set.add("including");
					// oracle
					set.add("increment");
					// mysql, oracle
					set.add("index");
					// oracle
					set.add("indexed");
					// oracle
					set.add("indexes");

					set.add("indicator");
					// oracle
					set.add("ind_partition");
					// oracle
					set.add("initial");

					set.add("initially");
					// oracle
					set.add("initrans");
					// mysql
					set.add("infile");

					set.add("inner");

					set.add("inout");

					set.add("input");

					set.add("insensitive");

					set.add("insert");
					// oracle
					set.add("instance");
					// oracle
					set.add("instances");
					// oracle
					set.add("instead");

					set.add("int");
					// mysql
					set.add("int1");
					// mysql
					set.add("int2");
					// mysql
					set.add("int3");
					// mysql
					set.add("int4");
					// mysql
					set.add("int8");

					set.add("integer");
					// oracle
					set.add("intermediate");

					set.add("intersect");

					set.add("interval");

					set.add("into");
					// mysql
					set.add("io_after_gtids");
					// mysql
					set.add("io_before_gtids");

					set.add("is");

					set.add("isolation");
					// oracle
					set.add("isolation_level");

					set.add("iterate");

					set.add("join");

					set.add("key");
					// mysql
					set.add("keys");
					// mysql
					set.add("kill");

					set.add("language");

					set.add("large");

					set.add("last");

					set.add("leading");

					set.add("leave");

					set.add("left");

					set.add("level");

					set.add("like");
					// mysql
					set.add("limit");
					//mysql
					set.add("linear");
					// mysql
					set.add("lines");
					// mysql
					set.add("load");

					set.add("local");

					set.add("localtime");

					set.add("localtimestamp");

					set.add("locator");
					// mysql
					set.add("lock");
					// mysql
					set.add("long");
					// mysql
					set.add("longlob");
					// mysql
					set.add("longtext");

					set.add("loop");

					set.add("lower");
					// mysql
					set.add("low_priority");

					set.add("map");
					// mysql
					set.add("master_bind");
					// mysql
					set.add("master_ssl_verify_server_cert");

					set.add("match");

					set.add("max");
					// mysql
					set.add("maxvalue");
					// mysql
					set.add("mediumblob");
					// mysql
					set.add("mediumint");
					// mysql
					set.add("mediumtext");

					set.add("member");

					set.add("merge");

					set.add("method");
					// mysql
					set.add("middleint");

					set.add("min");

					set.add("minute");
					// mysql
					set.add("minute_microsecond");
					// mysql
					set.add("minute_second");
					// mysql
					set.add("mod");

					set.add("modifies");

					set.add("module");

					set.add("month");

					set.add("names");

					set.add("national");

					set.add("natural");

					set.add("nchar");

					set.add("nclob");

					set.add("new");

					set.add("next");

					set.add("no");

					set.add("none");

					set.add("not");
					// mysql
					set.add("no_write_to_binlog");

					set.add("null");

					set.add("nullif");

					set.add("numeric");

					set.add("object");

					set.add("octet_length");

					set.add("of");

					set.add("old");

					set.add("on");

					set.add("only");

					set.add("open");
					// mysql
					set.add("optimize");

					set.add("option");
					// mysql
					set.add("optionally");

					set.add("or");

					set.add("order");

					set.add("ordinality");

					set.add("out");

					set.add("outer");

					set.add("output");
					// mysql
					set.add("outfile");

					set.add("over");

					set.add("overlaps");

					set.add("pad");

					set.add("parameter");

					set.add("partial");

					set.add("partition");

					set.add("path");

					set.add("position");

					set.add("precesion");

					set.add("prepare");

					set.add("preserve");

					set.add("primary");

					set.add("prior");

					set.add("privileges");

					set.add("procedure");

					set.add("public");
					// mysql
					set.add("purge");

					set.add("range");

					set.add("read");

					set.add("reads");
					// mysql
					set.add("read_write");

					set.add("real");

					set.add("recursive");

					set.add("ref");

					set.add("references");

					set.add("referencing");
					// mysql
					set.add("regexp");

					set.add("relative");

					set.add("release");
					// mysql
					set.add("rename");
					set.add("repeat");
					set.add("replace");
					set.add("require");
					set.add("resignal");
					set.add("restrict");
					set.add("return");
					set.add("revoke");
					set.add("right");
					set.add("rlike");
					set.add("schema");
					set.add("schemas");
					set.add("second_microsecond");
					set.add("select");
					set.add("sensitive");
					set.add("separator");
					set.add("set");
					set.add("show");
					set.add("signal");
					set.add("smallint");
					set.add("spatial");
					set.add("specific");
					set.add("sql");
					set.add("sqlexception");
					set.add("sqlstate");
					set.add("sqlwarning");
					set.add("sql_big_result");
					set.add("sql_calc_found_rows");
					set.add("sql_small_result");
					set.add("ssl");
					set.add("starting");
					set.add("straight_join");
					set.add("table");
					set.add("terminated");
					set.add("then");
					set.add("tinyblob");
					set.add("tinyint");
					set.add("tinytext");
					set.add("to");
					set.add("trailing");
					set.add("trigger");
					set.add("true");
					set.add("undo");
					set.add("union");
					set.add("unique");
					set.add("unlock");
					set.add("unsigned");
					set.add("update");
					set.add("usage");
					set.add("use");
					set.add("using");
					set.add("utc_date");
					set.add("utc_time");
					set.add("utc_timestamp");
					set.add("values");
					set.add("varbinary");
					set.add("varchar");
					set.add("varcharacter");
					set.add("varying");
					set.add("when");
					set.add("where");
					set.add("while");
					set.add("with");
					set.add("write");
					set.add("xor");
					set.add("year_month");
					set.add("zerofill");
					reservedWords = set;
				}
			}
		}
		return reservedWords;
	}

	public char getReservedWordEscapingBraceOpen() {
		return '\"';
	}

	public char getReservedWordEscapingBraceClose() {
		return '\"';
	}

}
