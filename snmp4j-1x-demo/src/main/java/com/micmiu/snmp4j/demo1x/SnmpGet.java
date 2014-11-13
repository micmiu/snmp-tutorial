package com.micmiu.snmp4j.demo1x;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * 演示： GET单个OID值
 * 
 * blog http://www.micmiu.com
 * 
 * @author Michael
 */
public class SnmpGet {

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

	public static void snmpGet(String ip, String community, String oid) {

		CommunityTarget target = createDefault(ip, community);
		Snmp snmp = null;
		try {
			PDU pdu = new PDU();
			// pdu.add(new VariableBinding(new OID(new int[]
			// {1,3,6,1,2,1,1,2})));
			pdu.add(new VariableBinding(new OID(oid)));

			DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			snmp.listen();
			System.out.println("-------> 发送PDU <-------");
			pdu.setType(PDU.GET);
			ResponseEvent respEvent = snmp.send(pdu, target);
			System.out.println("PeerAddress:" + respEvent.getPeerAddress());
			PDU response = respEvent.getResponse();

			if (response == null) {
				System.out.println("response is null, request time out");
			} else {

				// Vector<VariableBinding> vbVect =
				// response.getVariableBindings();
				// System.out.println("vb size:" + vbVect.size());
				// if (vbVect.size() == 0) {
				// System.out.println("response vb size is 0 ");
				// } else {
				// VariableBinding vb = vbVect.firstElement();
				// System.out.println(vb.getOid() + " = " + vb.getVariable());
				// }

				System.out.println("response pdu size is " + response.size());
				for (int i = 0; i < response.size(); i++) {
					VariableBinding vb = response.get(i);
					System.out.println(vb.getOid() + " = " + vb.getVariable());
				}

			}
			System.out.println("SNMP GET one OID value finished !");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SNMP Get Exception:" + e);
		} finally {
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException ex1) {
					snmp = null;
				}
			}

		}
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String ip = "192.168.8.254";
		String community = "public";
		String oidval = "1.3.6.1.2.1.1.1.0";
		SnmpGet.snmpGet(ip, community, oidval);

	}

}
