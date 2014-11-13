package com.micmiu.snmp4j.demo1x;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.util.WorkerPool;

/**
 * 演示：异步GET OID值
 * 
 * blog http://www.micmiu.com
 * 
 * @author Michael
 * 
 */
public class SnmpWalkMuliTAsyn {

	public static final int DEFAULT_VERSION = SnmpConstants.version2c;
	public static final String DEFAULT_PROTOCOL = "udp";
	public static final int DEFAULT_PORT = 161;
	public static final long DEFAULT_TIMEOUT = 3 * 1000L;
	public static final int DEFAULT_RETRY = 3;

	/**
	 * 创建对象communityTarget
	 * 
	 * @param ip
	 * @param community
	 * @return CommunityTarget
	 */
	public static CommunityTarget createDefault(String ip, String community) {
		Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
				+ "/" + DEFAULT_PORT);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setAddress(address);
		target.setVersion(DEFAULT_VERSION);
		target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
		target.setRetries(DEFAULT_RETRY);
		return target;
	}

	/**
	 * 异步采集信息
	 * 
	 * @param ip
	 * @param community
	 * @param oid
	 */
	public static void snmpAsynWalk(String ip, String community, String oid) {
		final CommunityTarget target = createDefault(ip, community);
		DefaultUdpTransportMapping transport = null;
		Snmp snmp = null;
		try {
			System.out.println("----> demo start <----");
			WorkerPool threadPool = ThreadPool.create("TestSNMPWorkPool", 2);
			MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(
					threadPool, new MessageDispatcherImpl());
			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(dispatcher, transport);
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
			snmp.listen();

			final PDU pdu = new PDU();
			final OID targetOID = new OID(oid);
			final CountDownLatch latch = new CountDownLatch(1);
			pdu.add(new VariableBinding(targetOID));

			ResponseListener listener = new ResponseListener() {
				public void onResponse(ResponseEvent event) {
					((Snmp) event.getSource()).cancel(event.getRequest(), this);

					try {
						PDU response = event.getResponse();
						// PDU request = event.getRequest();
						// System.out.println("[request]:" + request);
						if (response == null) {
							System.out.println("[ERROR]: response is null");
						} else if (response.getErrorStatus() != 0) {
							System.out.println("[ERROR]: response status"
									+ response.getErrorStatus() + " Text:"
									+ response.getErrorStatusText());
						} else {
							System.out
									.println("Received Walk response value :");
							VariableBinding vb = response.get(0);

							boolean finished = checkWalkFinished(targetOID,
									pdu, vb);
							if (!finished) {
								System.out.println(vb.getOid() + " = "
										+ vb.getVariable());
								pdu.setRequestID(new Integer32(0));
								pdu.set(0, vb);
								((Snmp) event.getSource()).getNext(pdu, target,
										null, this);
							} else {
								System.out
										.println("SNMP Asyn walk OID value success !");
								latch.countDown();
								// ((Snmp) event.getSource()).close();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						latch.countDown();
					}

				}
			};

			snmp.getNext(pdu, target, null, listener);
			System.out.println("asynchronous send pdu wait for response...");

			boolean wait = latch.await(30, TimeUnit.SECONDS);
			System.out.println("latch.await =:" + wait);
			snmp.close();
			System.out.println("----> demo end <----");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SNMP Asyn Walk Exception:" + e);
		}

	}

	private static boolean checkWalkFinished(OID walkOID, PDU pdu,
			VariableBinding vb) {
		boolean finished = false;
		if (pdu.getErrorStatus() != 0) {
			System.out.println("[true] pdu.getErrorStatus() != 0 ");
			System.out.println(pdu.getErrorStatusText());
			finished = true;
		} else if (vb.getOid() == null) {
			System.out.println("[true] vb.getOid() == null");
			finished = true;
		} else if (vb.getOid().size() < walkOID.size()) {
			System.out.println("[true] vb.getOid().size() < targetOID.size()");
			finished = true;
		} else if (walkOID.leftMostCompare(walkOID.size(), vb.getOid()) != 0) {
			System.out.println("[true] targetOID.leftMostCompare() != 0");
			finished = true;
		} else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
			System.out
					.println("[true] Null.isExceptionSyntax(vb.getVariable().getSyntax())");
			finished = true;
		} else if (vb.getOid().compareTo(walkOID) <= 0) {
			System.out.println("[true] vb.getOid().compareTo(walkOID) <= 0 ");
			finished = true;
		}
		return finished;

	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String ip = "192.168.8.254";
		String community = "public";

		List<String> oidList = new ArrayList<String>();
		oidList.add(".1.3.6.1.2.1.1.1.0");
		oidList.add(".1.3.6.1.2.1.1.3.0");
		oidList.add(".1.3.6.1.2.1.1.5.0");
		// 异步采集数据
		SnmpWalkMuliTAsyn.snmpAsynWalk(ip, community, "1.3.6.1.2.1.1");

	}
}
