package br.ufu.facom.network.translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.Controller;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceService;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFType;
import org.openflow.util.HexString;


public class GenericTranslator implements IOFMessageListener, IFloodlightModule {
	private IFloodlightProviderService floodlightProvider;
	private AppListener listener = null;
	private IStorageSourceService storageSource;

	static private GenericTranslator[] singleton = new GenericTranslator[1];

	static public GenericTranslator getInstance() throws InterruptedException {
		synchronized (singleton) {
			while (singleton[0] == null)
				singleton.wait();
		}
		return singleton[0];
	}

	public GenericTranslator() {
		synchronized (singleton) {
			if (singleton[0] != null) {
				throw new RuntimeException("Tried to instantiate "
						+ GenericTranslator.class.getName()
						+ " more than once!");
			}
			singleton[0] = this;
			singleton.notifyAll();
		}
	}

	public void addListener(AppListener listener) {
		synchronized (this) {
			this.listener = listener;
			this.notifyAll();
		}
	}

	public IFloodlightProviderService getFloodlightProvider() {
		return floodlightProvider;
	}

	@Override
	public String getName() {
		return "jslee-ra";
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
		l.add(IStorageSourceService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// There are lots of FloodLight services that could be useful for the
		// implementation
		// we intent doing with JSLEE, but above that layer, FloodLight have no
		// power there,
		// so, there is very little to do here, either...
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		// Register what to listen...
		// The commented out messages are not sent by the switch.
		OFType to_receive[] = { OFType.HELLO, OFType.ERROR,
				OFType.ECHO_REQUEST, OFType.ECHO_REPLY, OFType.VENDOR,
				// OFType.FEATURES_REQUEST,
				OFType.FEATURES_REPLY,
				// OFType.GET_CONFIG_REQUEST,
				OFType.GET_CONFIG_REPLY,
				// OFType.SET_CONFIG,
				OFType.PACKET_IN, OFType.FLOW_REMOVED, OFType.PORT_STATUS,
				// OFType.PACKET_OUT,
				// OFType.FLOW_MOD,
				// OFType.PORT_MOD,
				// OFType.STATS_REQUEST,
				OFType.STATS_REPLY,
				// OFType.BARRIER_REQUEST,
				OFType.BARRIER_REPLY, };
		for (OFType t : to_receive) {
			floodlightProvider.addOFMessageListener(t, this);
		}
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		
		if (listener == null) {
			synchronized (this) {
				while (listener == null)
					try {
						this.wait(0);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
			}
		}
		listener.sendEvent(sw.getId(), msg, cntx);
		
		return Command.CONTINUE;
	}

	public void writeMessage(Long sw, OFMessage msg) {
		try {
			List<OFMessage> msgs = new ArrayList<OFMessage>(1);
			msgs.add(msg);
			floodlightProvider.getSwitches().get(sw).write(msgs, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*****************************************************************************************************************
	 * Obter informações sobre todas as portas de cada SWITCH conectada, obetendo as seguintes informações abaixo:
	 * 
	 *         - Porta do Swith;
	 *         - Numero da Porta;
	 * 		   - Endereço da Porta do Hardware;
	 *         - Nome da Porta.
	 *  
	 * @return
	 * Adaptação do código :  GIOVANI FRANCISCO DE SANT`ANNA 
	 * 
	 *****************************************************************************************************************
	 */

    public Map<Long, List<OFPhysicalPort>> getSwitchesPorts(){
    	String[] portColumns = { Controller.PORT_SWITCH, Controller.PORT_NUMBER, Controller.PORT_HARDWARE_ADDRESS, Controller.PORT_NAME };
    	
    	Map<Long, List<OFPhysicalPort>> switchesPorts = new HashMap<Long, List<OFPhysicalPort>>();
    	IResultSet portResultSet = null;
    	portResultSet = storageSource.executeQuery(Controller.PORT_TABLE_NAME, portColumns, null, null);
    	
    	try {
    		
    		while(portResultSet.next()) {
    			
    			Long switchId = HexString.toLong(portResultSet.getString(Controller.PORT_SWITCH));
    			List<OFPhysicalPort> ports = switchesPorts.get(switchId);
    			
    			if(ports == null) {
    				ports = new ArrayList<OFPhysicalPort>();
    			}

    			OFPhysicalPort ofPhysicalPort = new OFPhysicalPort();
    			
    			ofPhysicalPort.setPortNumber((short)portResultSet.getInt(Controller.PORT_NUMBER));
    			ofPhysicalPort.setName(portResultSet.getString(Controller.PORT_NAME));
    			String portMac = portResultSet.getString(Controller.PORT_HARDWARE_ADDRESS);
    			ofPhysicalPort.setHardwareAddress(HexString.fromHexString(portMac));
    			
    			ports.add(ofPhysicalPort);
    			switchesPorts.put(switchId, ports);
    		}
    		
    	} finally {
    		portResultSet.close();
    	}
    	
    	return switchesPorts;
    }
	
}