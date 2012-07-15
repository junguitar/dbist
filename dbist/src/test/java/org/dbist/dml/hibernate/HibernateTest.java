package org.dbist.dml.hibernate;

import org.junit.Test;

public class HibernateTest {

	@Test
	public void test() {
		//		List<Dialect> dialectList = ValueUtils.toList(new MySQL5InnoDBDialect(), new Oracle10gDialect(), new SQLServerDialect(), new DB2Dialect());
		//		List<LockMode> lockModeList = ValueUtils.toList(LockMode.values());
		//		for (LockMode lockMode : lockModeList) {
		//			System.out.println("\r\nLockMode: " + lockMode.name());
		//			LockOptions lockOptions = new LockOptions(lockMode);
		//			lockOptions.setTimeOut(10000);
		//			for (Dialect dialect : dialectList) {
		//				SimpleSelect select = new SimpleSelect(dialect);
		//				select.addColumn("id");
		//				select.setTableName("BLOG");
		//				select.addCondition("id", "in :id");
		//				select.setLockOptions(lockOptions);
		//				System.out.println(dialect.getClass().getSimpleName() + ":\r\n" + select.toStatementString());
		//			}
		//		}
	}
}
