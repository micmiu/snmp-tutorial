package com.micmiu.tutorial.jrobin;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jrobin.core.Util;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraphDefTemplate;
import org.xml.sax.InputSource;

/**
 * 演示: JRobin绘制指定时间段的流量图
 * <p/>
 * blog: http://www.micmiu.com/enterprise-app/snmp/jrobin-graph-rpn/
 *
 * @author Michael
 */
public class GraphDemoHandler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String rootPath = "/Users/micmiu/no_sync/testdata/jrobin/";
			String rrdName = rootPath + "demo_port1.rrd";
			String rrdName2 = rootPath + "demo_port2.rrd";
			long startTime = Util.getTimestamp(2012, 3 - 1, 1);
			long endTime = Util.getTimestamp(2012, 4 - 1, 1);

			System.out.println("========== 基本的流量图 ==========");
			String imgFileName = rootPath + "jrobin_port1_1.png";
			RrdGraphDef rgdef = GraphDemoHandler.graphDef4Demo(startTime,
					endTime, rrdName, imgFileName);
			GraphDemoHandler.graphByDef(rgdef);

			imgFileName = rootPath + "jrobin_port2_1.png";
			RrdGraphDef rgdef2 = GraphDemoHandler.graphDef4Demo(startTime,
					endTime, rrdName2, imgFileName);
			GraphDemoHandler.graphByDef(rgdef2);

			System.out.println("========== 指定时间段的流量图 ==========");
			imgFileName = rootPath + "jrobin_port1_2.png";
			String time1 = "1330531200";// 2012-03-01 00:00:00
			String time2 = "1331827200";// 2012-03-16 00:00:00
			String time3 = "1333209601";// 2012-04-01 00:00:01

			RrdGraphDef rgdef3 = GraphDemoHandler.graphDef4Interval(startTime,
					endTime, rrdName, imgFileName, time1, time2);
			GraphDemoHandler.graphByDef(rgdef3);

			imgFileName = rootPath + "jrobin_port2_2.png";
			RrdGraphDef rgdef4 = GraphDemoHandler.graphDef4Interval(startTime,
					endTime, rrdName2, imgFileName, time2, time3);
			GraphDemoHandler.graphByDef(rgdef4);

			System.out.println("========== 不同端口不同时间段的合并图 ==========");
			imgFileName = rootPath + "jrobin_port12_merge.png";
			List<String[]> rrdparas = new ArrayList<String[]>();
			rrdparas.add(new String[]{rrdName, time1, time2});
			rrdparas.add(new String[]{rrdName2, time2, time3});

			RrdGraphDef rgdef5 = GraphDemoHandler.graphDef4HorizontalMerge(
					startTime, endTime, imgFileName, rrdparas);
			GraphDemoHandler.graphByDef(rgdef5);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static RrdGraphDef cfgBaseGraphDef() {
		RrdGraphDef rgdef = new RrdGraphDef();
		rgdef.setSignature("www.micmiu.com");
		rgdef.setAntiAliasing(false);
		rgdef.setImageFormat("PNG");
		rgdef.setSmallFont(new Font("Monospaced", Font.PLAIN, 11));
		rgdef.setLargeFont(new Font("SansSerif", Font.BOLD, 14));
		// rgdef

		rgdef.setTitle("JRobin Graph Demo for micmiu.com");
		rgdef.setBase(1024);
		rgdef.setWidth(500);
		rgdef.setHeight(200);
		rgdef.setVerticalLabel("transfer speed [bps]");

		return rgdef;
	}

	/**
	 * 自定义画图模板
	 *
	 * @param startTime
	 * @param endTime
	 * @param rrdName
	 * @param imgFileName
	 * @return
	 */
	public static RrdGraphDef graphDef4Demo(long startTime, long endTime,
											String rrdName, String imgFileName) {
		RrdGraphDef rgdef = null;
		try {
			System.out.println("[rrd graph by RrdGraphDef start...]");
			rgdef = cfgBaseGraphDef();

			rgdef.setFilename(imgFileName);
			rgdef.setStartTime(startTime);
			rgdef.setEndTime(endTime);

			rgdef.datasource("port", rrdName, "flow", "AVERAGE");
			rgdef.datasource("line_data", "port,8,*");
			rgdef.line("line_data", Color.BLUE, "test flow\\l");

			// \\l->左对齐 \\c->中间对齐 \\r->右对齐 \\j->自适应
			// \\s-> \\g->glue \\J->
			rgdef.gprint("line_data", "MAX", "速率最大值=%.2f %sbps");
			rgdef.gprint("line_data", "AVERAGE", "速率平均值=%.2f %sbps\\l");
			rgdef.gprint("line_data", "TOTAL", "总流量=%.2f %sb\\l");
			rgdef.comment("More info see : www.micmiu.com");

			return rgdef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 创建指定时间段的画图模板
	 *
	 * @param startTime
	 * @param endTime
	 * @param rrdName
	 * @param imgFileName
	 * @return
	 */
	public static RrdGraphDef graphDef4Interval(long startTime, long endTime,
												String rrdName, String imgFileName, String time1, String time2) {
		RrdGraphDef rgdef = null;
		try {
			System.out.println("[rrd graph def for TimeInterval start...]");
			rgdef = cfgBaseGraphDef();

			rgdef.setFilename(imgFileName);
			rgdef.setStartTime(startTime);
			rgdef.setEndTime(endTime);

			rgdef.datasource("port", rrdName, "flow", "AVERAGE");
			// rpn:TIME,starttime,GE,TIME,endtime,LT,*,dsname,UNKN,IF
			String rpnExp = "TIME,{0},GE,TIME,{1},LE,*,{2},UNKN,IF";
			String rpn = MessageFormat.format(rpnExp, time1, time2, "port");
			rgdef.datasource("port_data", rpn);
			rgdef.datasource("line_data", "port_data,8,*");
			rgdef.line("line_data", Color.BLUE, "test flow\\l");

			rgdef.gprint("line_data", "MAX", "速率最大值=%.2f %sbps");
			rgdef.gprint("line_data", "AVERAGE", "速率平均值=%.2f %sbps\\l");
			rgdef.gprint("line_data", "TOTAL", "总流量=%.2f %sb\\l");
			rgdef.comment("More info see : www.micmiu.com");

			return rgdef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 多个端口指定时间段合并画图模板
	 *
	 * @param startTime
	 * @param endTime
	 * @param imgFileName
	 * @param rrdparas
	 * @return
	 */
	public static RrdGraphDef graphDef4HorizontalMerge(long startTime, long endTime, String imgFileName, List<String[]> rrdparas) {
		RrdGraphDef rgdef = null;
		try {
			System.out.println("[ RrdGraphDef for horizontal merge start...]");
			rgdef = cfgBaseGraphDef();

			rgdef.setFilename(imgFileName);
			rgdef.setStartTime(startTime);
			rgdef.setEndTime(endTime);

			// rpn:TIME,starttime,GE,TIME,endtime,LT,*,port_1,0,IF
			String rpnExp = "TIME,{0},GE,TIME,{1},LT,*,{2},0,IF";
			String totalExp = "0";
			for (int i = 0; i < rrdparas.size(); i++) {
				rgdef.datasource("port_" + i, rrdparas.get(i)[0], "flow",
						"AVERAGE");
				String rpn = MessageFormat.format(rpnExp, rrdparas.get(i)[1],
						rrdparas.get(i)[2], "port_" + i);
				rgdef.datasource("port_" + i + "_data", rpn);
				rgdef.datasource("port_" + i + "_data_8", "port_" + i
						+ "_data,8,*");
				totalExp += ",port_" + i + "_data_8,+";

			}
			rgdef.datasource("port_merge", totalExp);

			rgdef.line("port_merge", Color.BLUE, "test flow\\l");
			rgdef.gprint("port_merge", "MAX", "速率最大值=%.2f %sbps");
			rgdef.gprint("port_merge", "AVERAGE", "速率平均值=%.2f %sbps\\l");
			rgdef.gprint("port_merge", "TOTAL", "总流量=%.2f %sb\\l");
			rgdef.comment("More info see : www.micmiu.com");

			return rgdef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void graphByDef(RrdGraphDef rgdef) {
		try {

			RrdGraph graph = new RrdGraph(rgdef);
			System.out.println("[rrd graph info:]"
					+ graph.getRrdGraphInfo().dump());

			// 如果filename没有设置，只是在内存中，可以调用下面的方法再次生成图片文件
			if ("-".equals(graph.getRrdGraphInfo().getFilename())) {
				createImgFile(graph, "jrobin-graph.jpg");
			}
			System.out.println("[Jrobin graph by RrdGraphDef success.]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 根据定义的XML文件创建画图模板
	 *
	 * @param startTime
	 * @param endTime
	 * @param templateName
	 * @param rrdName
	 * @param imgFile
	 * @return
	 */
	public static RrdGraphDef createGraphDefByTemplate(long startTime, long endTime, String templateName, String rrdName, String imgFile) {
		try {
			System.out
					.println("[GraphDef create by xml template file start...]");
			RrdGraphDefTemplate defTemplate = new RrdGraphDefTemplate(
					new InputSource(templateName));
			// setVariable 设置XML template的变量
			defTemplate.setVariable("startTime", startTime);
			defTemplate.setVariable("endTime", endTime);
			defTemplate.setVariable("img_file", imgFile);
			defTemplate.setVariable("rrd_file", rrdName);
			return defTemplate.getRrdGraphDef();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ImageIO 生成图片文件
	 */
	public static void createImgFile(RrdGraph graph, String imgFileName) {

		try {
			BufferedImage image = new BufferedImage(graph.getRrdGraphInfo()
					.getWidth(), graph.getRrdGraphInfo().getHeight(),
					BufferedImage.TYPE_INT_RGB);
			graph.render(image.getGraphics());
			File imgFile = new File(imgFileName);
			ImageIO.write(image, "PNG", imgFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}