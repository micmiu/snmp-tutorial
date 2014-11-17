package com.micmiu.tutorial.jrobin;

import org.jrobin.core.DsDef;
import org.jrobin.core.DsTypes;
import org.jrobin.core.FetchData;
import org.jrobin.core.FetchRequest;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.Sample;
import org.jrobin.core.Util;

/**
 * SNMP之JRobin Core学习
 * site: http://www.micmiu.com/enterprise-app/snmp/snmp-jrobin-core-demo/
 * User: <a href="http://micmiu.com">micmiu</a>
 * Date: 11/13/2014
 * Time: 17:32
 */
public class TestCoreRrd {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 2010-10-01:1285862400L 2010-11-01:1288540800L
		long startTime = Util.getTimestamp(2010, 10 - 1, 1);
		long endTime = Util.getTimestamp(2010, 11 - 1, 1);

		TestCoreRrd test = new TestCoreRrd();
		String rootPath = "/Users/micmiu/no_sync/testdata/jrobin/";
		String rrdName = "demo_flow.rrd";
		// 测试创建RrdDef
		RrdDef rrdDef = test.createRrdDef(rootPath, rrdName, startTime);
		// 测试创建RRD文件 初始数据
		test.createRRDInitData(startTime, endTime, rootPath, rrdName, rrdDef);
		// 测试获取RrdDb的方法
		test.getRrdDbMethod(rootPath);
		// 测试FetchData获取RRD
		test.fetchRrdData(rootPath, rrdName);
	}

	/**
	 * 创建RRDDef
	 */
	private RrdDef createRrdDef(String rootPath, String rrdName, long startTime) {
		try {

			String rrdPath = rootPath + rrdName;
			RrdDef rrdDef = new RrdDef(rrdPath, startTime - 1, 300);
			// DsTypes: GAUGE COUNTER DERIVE ABSOLUTE
			DsDef dsDef = new DsDef("input", DsTypes.DT_COUNTER, 600, 0,
					Double.NaN);
			rrdDef.addDatasource(dsDef);

			rrdDef.addDatasource("output", DsTypes.DT_COUNTER, 600, 0,
					Double.NaN);

			rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
			rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
			rrdDef.addArchive("AVERAGE", 0.5, 24, 797);
			rrdDef.addArchive("AVERAGE", 0.5, 288, 775);
			rrdDef.addArchive("MAX", 0.5, 1, 600);
			rrdDef.addArchive("MAX", 0.5, 6, 700);
			rrdDef.addArchive("MAX", 0.5, 24, 797);
			rrdDef.addArchive("MAX", 0.5, 288, 775);

			// RRD file definition is completed

			rrdDef.exportXmlTemplate(rootPath + rrdName + "_template.xml");
			System.out.println("[RrdDef Template  export xml success]");

			return rrdDef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通过RrdDef创建RRD文件并初始化数据
	 */
	private void createRRDInitData(long startTime, long endTime,
								   String rootPath, String rrdName, RrdDef rrdDef) {
		try {

			RrdDb rrdDb = new RrdDb(rrdDef);
			// / by this point, rrd file can be found on your disk

			// 模拟一些测试数据
			//Math.sin(2 * Math.PI * (t / 86400.0)) * baseval;
			int baseval = 50;
			for (long t = startTime; t < endTime; t += 300) {
				Sample sample = rrdDb.createSample(t);
				double tmpval = Math.random() * baseval;
				double tmpval2 = Math.random() * baseval;
				sample.setValue("input", tmpval + 50);
				sample.setValue("output", tmpval2 + 50);
				sample.update();
			}
			System.out.println("[RrdDb init data success]");
			System.out.println("[Rrd path]:" + rrdDef.getPath());

			// rrdDb.dumpXml(rootPath + rrdName + "_rrd.xml")
			rrdDb.exportXml(rootPath + rrdName + ".xml");

			// If your RRD files are updated rarely, open them only when
			// necessary and close them as soon as possible.
			rrdDb.close();

			System.out.println("[RrdDb export xml success]");
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 除根据RrdDef以外获取RrdDb的其他方法
	 */
	private void getRrdDbMethod(String rootPath) {
		try {

			// 根据RRD文件获取RrdDb
			String rrdFullPath = rootPath + "demo_flow.rrd";
			RrdDb rrdDb = new RrdDb(rrdFullPath);
			System.out.println("[info:]" + rrdDb.getInfo() + "[path:]"
					+ rrdDb.getPath());
			rrdDb.close();

			// 根据XML文件获取RrdDb
			rrdDb = new RrdDb(rootPath + "copy.rrd", rootPath
					+ "demo_flow.rrd.xml");
			System.out.println("[info:]" + rrdDb.getInfo() + "[path:]"
					+ rrdDb.getPath());
			rrdDb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	private void fetchRrdData(String rootPath, String rrdName) {
		try {
			// open the file
			RrdDb rrd = new RrdDb(rootPath + rrdName);

			// create fetch request using the database reference
			FetchRequest request = rrd.createFetchRequest("AVERAGE", Util
					.getTimestamp(2010, 10 - 1, 1), Util.getTimestamp(2010,
					10 - 1, 2));

			System.out.println("[requet dump:]" + request.dump());

			// filter the datasources you really need
			// String[] filterDataSource = { "input", "output" };
			// request.setFilter(filterDataSource);

			// if you want only the "input" datasource use:
			// request.setFilter("input");

			// execute the request
			FetchData fetchData = request.fetchData();
			int columnCount = fetchData.getColumnCount();
			int rowCount = fetchData.getRowCount();
			long[] timestamps = fetchData.getTimestamps();
			System.out.println("[data column count:]" + columnCount);
			System.out.println("[data row count:]" + rowCount);

			// System.out.println("[fetch data dump:]" + fetchData.dump());
			// 循环获取数据
			double[][] values = fetchData.getValues();
			StringBuffer buffer = new StringBuffer("");
			for (int row = 0; row < rowCount; row++) {
				buffer.append(timestamps[row]);
				buffer.append(":  ");
				for (int dsIndex = 0; dsIndex < columnCount; dsIndex++) {
					buffer.append(Util.formatDouble(values[dsIndex][row]));
					buffer.append("  ");
				}
				buffer.append("\n");
			}
			System.out.println("[fetch data display :]\n" + buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}