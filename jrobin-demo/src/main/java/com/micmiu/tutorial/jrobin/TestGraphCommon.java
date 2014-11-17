package com.micmiu.tutorial.jrobin;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.jrobin.core.Util;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraphDefTemplate;
import org.xml.sax.InputSource;

/**
 * SNMP之JRobin画图
 * blog: http://www.micmiu.com/enterprise-app/snmp/snmp-jrobin-graph-demo/
 * <p/>
 * User: <a href="http://micmiu.com">micmiu</a>
 * Date: 11/17/2014
 * Time: 14:25
 */
public class TestGraphCommon {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rootPath = "/Users/micmiu/no_sync/testdata/jrobin/";
		String imgFileName = "demo_graph_rrd.png";

		TestGraphCommon test = new TestGraphCommon();

		// 测试直接定义画图模板生成图片
		test.graphByGraphDef(rootPath, imgFileName);

		String tempFileName = "graph-def-template.xml";
		// 测试根据定义的XML模板文件生成图片
		test.graphByTemplate(rootPath, tempFileName);
	}

	/**
	 * 直接定义画图模板生成图片
	 *
	 * @param rootPath
	 * @param imgFileName
	 */
	private void graphByGraphDef(String rootPath, String imgFileName) {
		try {
			System.out.println("[rrd graph by RrdGraphDef start...]");
			// 2010-10-01:1285862400L 2010-11-01:1288540800L
			long startTime = Util.getTimestamp(2010, 10 - 1, 1);
			long endTime = Util.getTimestamp(2010, 10 - 1, 31);

			RrdGraphDef rgdef = new RrdGraphDef();
			// If the filename is set to '-' the image will be created only in
			// memory (no file will be created).
			// rgdef.setFilename("-");
			rgdef.setFilename(rootPath + imgFileName);

			// "PNG", "GIF" or "JPG"
			rgdef.setImageFormat("PNG");
			// rgdef.setTimeSpan(startTime, endTime);
			rgdef.setStartTime(startTime);
			rgdef.setEndTime(endTime);

			rgdef.setAntiAliasing(false);
			rgdef.setSmallFont(new Font("Monospaced", Font.PLAIN, 11));
			rgdef.setLargeFont(new Font("SansSerif", Font.BOLD, 14));

			rgdef.setTitle("JRobin graph by RrdGraphDef demo");
			// default 400
			rgdef.setWidth(500);
			// default 100
			rgdef.setHeight(200);

			// 一般不需要设置这个参数
			// rgdef.setStep(86400);

			rgdef.setVerticalLabel("transfer speed [bits/sec]");

			rgdef.datasource("in", rootPath + "demo_flow.rrd", "input",
					"AVERAGE");
			rgdef.datasource("out", rootPath + "demo_flow.rrd", "output",
					"AVERAGE");
			rgdef.datasource("in8", "in,8,*");
			rgdef.datasource("out8", "out,8,*");
			// PS：先画域的再画线的，否则线会被域遮盖
			rgdef.area("out8", new Color(0, 206, 0), "output traffic");
			rgdef.line("in8", Color.BLUE, "input traffic\\l");

			// \\l->左对齐 \\c->中间对齐 \\r->右对齐 \\j->自适应
			// \\s-> \\g->glue \\J->
			rgdef.gprint("in8", "MAX", "maxIn=%.2f %sbits/sec");
			rgdef.gprint("out8", "MAX", "maxOut=%.2f %sbits/sec\\l");
			rgdef.gprint("in8", "AVERAGE", "avgIn=%.2f %sbits/sec");
			rgdef.gprint("out8", "AVERAGE", "avgOut=%.2f %sbits/sec\\l");
			rgdef.gprint("in8", "TOTAL", "totalIn=%.2f %sbits/sec");
			rgdef.gprint("out8", "TOTAL", "totalOut=%.2f %sbits/sec\\l");
			rgdef.comment("画图测试");

			RrdGraph graph = new RrdGraph(rgdef);
			System.out.println("[rrd graph info:]"
					+ graph.getRrdGraphInfo().dump());
			// 如果filename没有设置，只是在内存中，可以调用下面的方法再次生成图片文件
			if ("-".equals(graph.getRrdGraphInfo().getFilename())) {
				createImgFile(graph, rootPath + imgFileName);
			}
			System.out.println("[rrd graph by RrdGraphDef success.]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ImageIO 生成图片文件
	 */
	private void createImgFile(RrdGraph graph, String imgFileFullName) {

		try {
			BufferedImage image = new BufferedImage(graph.getRrdGraphInfo()
					.getWidth(), graph.getRrdGraphInfo().getHeight(),
					BufferedImage.TYPE_INT_RGB);
			graph.render(image.getGraphics());
			File imgFile = new File(imgFileFullName);
			ImageIO.write(image, "PNG", imgFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据定义的XML模板生成图片
	 *
	 * @param rootPath
	 * @param tempFileName
	 */
	private void graphByTemplate(String rootPath, String tempFileName) {
		try {
			System.out.println("[rrd graph by xml template start...]");
			RrdGraphDefTemplate defTemplate = new RrdGraphDefTemplate(
					new InputSource(rootPath + tempFileName));
			// setVariable 设置XML template的变量
			defTemplate.setVariable("startTime", "20101001 00:00");
			defTemplate.setVariable("endTime", "20101031 23:59");
			defTemplate.setVariable("in_rrd_file", rootPath + "demo_flow.rrd");
			defTemplate.setVariable("out_rrd_file", rootPath + "demo_flow.rrd");
			RrdGraph graph = new RrdGraph(defTemplate.getRrdGraphDef());

			BufferedImage image = new BufferedImage(graph.getRrdGraphInfo()
					.getWidth(), graph.getRrdGraphInfo().getHeight(),
					BufferedImage.TYPE_INT_RGB);
			graph.render(image.getGraphics());
			File imgFile = new File(rootPath + "demo_graph_tmp.PNG");
			ImageIO.write(image, "PNG", imgFile);//
			System.out.println("[rrd graph by xml template success.]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}