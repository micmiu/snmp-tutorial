package com.micmiu.snmp4j.demo1x;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

/**
 * 演示SNMP Trap多线程接收解析信息
 * 
 * @blog http://www.micmiu.com
 * @author Michael
 */
public class SnmpTrapMultiThreadReceiver implements CommandResponder {
	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;

	public SnmpTrapMultiThreadReceiver() {
	}

	private void init() throws UnknownHostException, IOException {
		threadPool = ThreadPool.create("TrapPool", 2);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());
		listenAddress = GenericAddress.parse(System.getProperty(
				"snmp4j.listenAddress", "udp:127.0.0.1/162"));
		TransportMapping transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping(
					(UdpAddress) listenAddress);
		} else {
			transport = new DefaultTcpTransportMapping(
					(TcpAddress) listenAddress);
		}
		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(
				MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		snmp.listen();
	}

	public void run() {
		System.out.println("----> Trap Receiver run ... <----");
		try {
			init();
			snmp.addCommandResponder(this);
			System.out.println("----> 开始监听端口，等待Trap message  <----");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void processPdu(CommandResponderEvent event) {
		System.out.println("----> 开始解析ResponderEvent: <----");
		if (event == null || event.getPDU() == null) {
			System.out.println("[Warn] ResponderEvent or PDU is null");
			return;
		}
		Vector<VariableBinding> vbVect = event.getPDU().getVariableBindings();
		for (VariableBinding vb : vbVect) {
			System.out.println(vb.getOid() + " = " + vb.getVariable());
		}
		System.out.println("---->  本次ResponderEvent 解析结束 <----");
	}

	public static void main(String[] args) {
		SnmpTrapMultiThreadReceiver trapReceiver = new SnmpTrapMultiThreadReceiver();
		trapReceiver.run();
	}
}
