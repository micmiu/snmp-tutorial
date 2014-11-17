package com.micmiu.tutorial.jrobin.data;

import org.jrobin.core.*;

import java.util.List;

/**
 * Created
 * User: <a href="http://micmiu.com">micmiu</a>
 * Date: 11/17/2014
 * Time: 11:05
 */
public class SimpleDataBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String rootPath = "/Users/micmiu/no_sync/testdata/jrobin/";
			String rrdName = rootPath + "demo_port1.rrd";
			long startTime = Util.getTimestamp(2012, 3 - 1, 1);
			long endTime = Util.getTimestamp(2012, 4 - 1, 1);

			RrdDef rrdDef = SimpleDataBuilder.createRrdDef4Demo(rrdName, startTime);
			System.out.println("RrdDef Demo create");
			rrdDef.exportXmlTemplate(rrdName + "_template.xml");
			System.out.println("[RrdDef Template  export to xml ]");
			SimpleDataBuilder.initData4Demo(rrdDef, startTime, endTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 创建RRDDef
	 */
	public static RrdDef createRrdDef4Demo(String rrdName, long startTime) {
		try {
			RrdDef rrdDef = new RrdDef(rrdName, startTime - 1, 300);
			// DsTypes: GAUGE COUNTER DERIVE ABSOLUTE
			DsDef dsDef = new DsDef("flow", DsTypes.DT_COUNTER, 600, 0,
					Double.NaN);
			rrdDef.addDatasource(dsDef);

			rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
			rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
			rrdDef.addArchive("AVERAGE", 0.5, 24, 797);
			rrdDef.addArchive("AVERAGE", 0.5, 288, 775);
			rrdDef.addArchive("MAX", 0.5, 1, 600);
			rrdDef.addArchive("MAX", 0.5, 6, 700);
			rrdDef.addArchive("MAX", 0.5, 24, 797);
			rrdDef.addArchive("MAX", 0.5, 288, 775);
			System.out.println("RrdDef info =:" + rrdDef.dump());
			return rrdDef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通过RrdDef创建RRD文件并初始化数据
	 */
	public static void initData4Demo(RrdDef rrdDef, long startTime, long endTime) {
		try {
			RrdDb rrdDb = new RrdDb(rrdDef);
			// by this point, rrd file can be found on your disk
			// 模拟一些测试数据
			// Math.sin(2 * Math.PI * (t / 86400.0)) * baseval;
			int baseval = 50;
			int dscount = rrdDef.getDsCount();
			for (long t = startTime; t < endTime; t += 300) {
				Sample sample = rrdDb.createSample(t);
				double ranval = Math.random() * baseval;
				for (int i = 0; i < dscount; i++) {
					sample.setValue(i, baseval + ranval);
				}
				sample.update();
			}
			System.out.println("[RrdDb init data success]");
			System.out.println("[Rrd path]:" + rrdDef.getPath());

			// rrdDb.dumpXml(rrdDef.getPath() + "_data.xml")
			rrdDb.exportXml(rrdDef.getPath() + "_data.xml");

			// If your RRD files are updated rarely, open them only when
			// necessary and close them as soon as possible.
			rrdDb.close();

			System.out.println("[RrdDb init demo data success]");
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 创建RRDDef
	 */
	public static RrdDef createRrdDef(String rrdName, long startTime,
									  List<DsDef> dsDefList) {
		try {
			RrdDef rrdDef = new RrdDef(rrdName, startTime - 1, 300);
			for (DsDef dsDef : dsDefList) {
				rrdDef.addDatasource(dsDef);
			}
			rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
			rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
			rrdDef.addArchive("AVERAGE", 0.5, 24, 797);
			rrdDef.addArchive("AVERAGE", 0.5, 288, 775);
			rrdDef.addArchive("MAX", 0.5, 1, 600);
			rrdDef.addArchive("MAX", 0.5, 6, 700);
			rrdDef.addArchive("MAX", 0.5, 24, 797);
			rrdDef.addArchive("MAX", 0.5, 288, 775);

			return rrdDef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 创建RRDDef
	 */
	public static RrdDef createFlowRrdDef(String rrdName, long startTime) {
		try {
			RrdDef rrdDef = new RrdDef(rrdName, startTime - 1, 300);

			// DsTypes: GAUGE COUNTER DERIVE ABSOLUTE
			DsDef dsDef = new DsDef("in", DsTypes.DT_COUNTER, 600, 0,
					Double.NaN);
			rrdDef.addDatasource(dsDef);
			rrdDef.addDatasource("out", DsTypes.DT_COUNTER, 600, 0, Double.NaN);
			rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
			rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
			rrdDef.addArchive("AVERAGE", 0.5, 24, 797);
			rrdDef.addArchive("AVERAGE", 0.5, 288, 775);
			rrdDef.addArchive("MAX", 0.5, 1, 600);
			rrdDef.addArchive("MAX", 0.5, 6, 700);
			rrdDef.addArchive("MAX", 0.5, 24, 797);
			rrdDef.addArchive("MAX", 0.5, 288, 775);

			return rrdDef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
