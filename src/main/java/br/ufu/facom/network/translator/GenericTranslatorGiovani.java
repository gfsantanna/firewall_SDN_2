     /*********************************************************************************************************
     * 
     *  FIREWALL Floodlight - Provedor de Serviços 
     * 
     * 
     */
package br.ufu.facom.network.translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.AppCookie;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.RoutingDecision;
import net.floodlightcontroller.util.OFMessageDamper;

public class GenericTranslatorGiovani implements IOFMessageListener, IFloodlightModule {

	private IFloodlightProviderService floodlightProvider;
	
    protected int subnet_mask = IPv4.toIPv4Address("255.255.255.0");

	protected static Logger log = LoggerFactory.getLogger(GenericTranslatorGiovani.class);

	public GenericTranslatorGiovani() {
	}

	public IFloodlightProviderService getFloodlightProvider() {
		return floodlightProvider;
	}

	@Override
	public String getName() {
		return "firewall-Giovani-Santanna";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// Maybe, who knows?
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// Maybe, too...
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// We don't export any services (whatever that means)
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// We don't have any services (I think)
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
			
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		 /*********************************************************************************************************
	     * 
	     *  
	     * Cadastrando os tipos de mensagens, para ouvir o que as mensagens comentadas não são enviados pelo switch.
	     * 
	     */
		OFType to_receive[] = { OFType.HELLO, OFType.ERROR, OFType.PACKET_IN, };
		for (OFType t : to_receive) {
			floodlightProvider.addOFMessageListener(t, this);
		}
	}
    /*********************************************************************************************************
     * 
     *  O Receive recebe o PACKT_in  e analisar os pacotes e que tipo de pacote.
     * /  (non-Javadoc)
     * 
     *      
     */
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {

		System.out.println(
				"######  Simulação do FIREWALL usando o controlador FLOODLIGHT ####### -------> OK  " + msg.toString());
		// @TODO fazer seu codigo
		/*********************************************************************************************************
	     * 
	     *  O Receive recebe o PACKT_in  e analisar os pacotes e que tipo de pacote.
	     *  
	     */
		switch (msg.getType()) {
		case PACKET_IN:
			/*********************************************************************************************************
		     * 
		     *  O Receive recebe o PACKT_in  e analisar os pacotes.
		     *  Bloqueia os serviços e portas.
		     */
			
			System.out.println("/n ####### O Pacote de Mensagem veio em... " + msg.toString());
			IRoutingDecision decision = null;
			if (cntx != null) {
				decision = IRoutingDecision.rtStore.get(cntx, IRoutingDecision.CONTEXT_DECISION);

				return this.processPacketFirewall(sw, (OFPacketIn) msg, decision, cntx);
			}

			break;
		default:
			break;
		}

		return Command.CONTINUE;
	}

    /**
     * Checks whether an IP address is a broadcast address or not (determines
     * using subnet mask)
     * 
     * @param IPAddress
     *            the IP address to check
     * @return true if it is a broadcast address, false otherwise
     */
    protected boolean IPIsBroadcast(int IPAddress) {
        // inverted subnet mask
        int inv_subnet_mask = ~this.subnet_mask;
        return ((IPAddress & inv_subnet_mask) == inv_subnet_mask);
    }
    

	private net.floodlightcontroller.core.IListener.Command processPacketFirewall(IOFSwitch sw, OFPacketIn pi,
			IRoutingDecision decision, FloodlightContext cntx) {
		// TODO Auto-generated method stub

		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		// if(eth.getEtherType() == Ethernet.TYPE_ARP){
		// System.out.println("chegou o pacote arp"); // Allowing L2 broadcast +
		// ARP broadcast
		// request (also deny malformed // broadcasts -> L2 broadcast + L3 *
		// unicast)
		if (eth.isBroadcast() == true) {
			boolean allowBroadcast = true; // the case to determine if we have
											// L2 broadcast + L3 unicast
			// don't allow this broadcast packet if such is the case (malformed
			// packet)
			if (eth.getEtherType() == Ethernet.TYPE_IPv4	&& this.IPIsBroadcast(((IPv4) eth.getPayload()).getDestinationAddress()) == false) {
				allowBroadcast = false;
			}
			if (allowBroadcast == true) {
				System.out.println("Allowing broadcast traffic for PacketIn={}" + pi);

				decision = new RoutingDecision(sw.getId(), pi.getInPort(),
						IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
						IRoutingDecision.RoutingAction.MULTICAST);
				decision.addToContext(cntx);
			} else {
				System.out.println("Blocking malformed broadcast traffic for PacketIn={}" + pi);

				decision = new RoutingDecision(sw.getId(), pi.getInPort(),
						IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
						IRoutingDecision.RoutingAction.DROP);
				decision.addToContext(cntx);
			}
			return Command.CONTINUE;
		}

		if (eth.getEtherType() == Ethernet.TYPE_IPv4) {
			IPacket pkt = eth.getPayload();
			IPv4 pkt_ip = (IPv4) pkt;
			String ipOrigem = IPv4.fromIPv4Address(pkt_ip.getDestinationAddress());

			System.out.println("ipOrigem: " + ipOrigem);

			TCP pkt_tcp = null;
			UDP pkt_udp = null;
			/// TRATA TCP
			if (pkt_ip.getProtocol() == IPv4.PROTOCOL_TCP) {
				pkt_tcp = (TCP) pkt_ip.getPayload();
				System.out.println("Pacote tcp: " + pkt_tcp.getDestinationPort());
				if (pkt_tcp.getDestinationPort() == 5000) { /// se porta for
															/// 5000 de destino,
															/// bloquea. Caso
															/// contrario
															/// libera.
					doDropFlow(sw, pi, decision, cntx);
					System.out.println("bloquear" + ipOrigem);
				} else {
					decision = new RoutingDecision(sw.getId(), pi.getInPort(),
							IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
							IRoutingDecision.RoutingAction.FORWARD);
					decision.setWildcards(OFMatch.OFPFW_ALL);
					decision.addToContext(cntx);
					System.out.println("liberado" + ipOrigem);
				}
			}
			// TRATA UDP
			if (pkt_ip.getProtocol() == IPv4.PROTOCOL_UDP) {
				pkt_udp = (UDP) pkt_ip.getPayload();
				System.out.println("Pacote udp: ");

			}
			if (pkt_ip.getProtocol() != IPv4.PROTOCOL_ICMP) {
				System.out.println("Pacote icmp: " + ipOrigem);
				decision = new RoutingDecision(sw.getId(), pi.getInPort(),
						IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
						IRoutingDecision.RoutingAction.DROP);
				decision.setWildcards(OFMatch.OFPFW_IN_PORT);
				decision.addToContext(cntx);
			}
		}
		return Command.CONTINUE;
	}

	protected void doDropFlow(IOFSwitch sw, OFPacketIn pi, IRoutingDecision decision, FloodlightContext cntx) {
		// initialize match structure and populate it using the packet
		OFMatch match = new OFMatch();
		match.loadFromPacket(pi.getPacketData(), pi.getInPort());
		match.setWildcards(OFMatch.OFPFW_TP_DST);

		// Create flow-mod based on packet-in and src-switch
		OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
		List<OFAction> actions = new ArrayList<OFAction>(); // Set no action to
															// drop
		long cookie = AppCookie.makeCookie(0, 0);

		fm.setCookie(cookie).setHardTimeout((short) 0).setIdleTimeout((short) 5).setBufferId(OFPacketOut.BUFFER_ID_NONE)
				.setMatch(match).setActions(actions).setLengthU(OFFlowMod.MINIMUM_LENGTH); // +OFActionOutput.MINIMUM_LENGTH);

		try {
			System.out.println("write drop flow-mod sw={} match={} flow-mod={}" + new Object[] { sw, match, fm });

			sw.write(fm, cntx);
		} catch (IOException e) {
			System.err.println("Failure writing drop flow mod" + e);
		}
	}
}
