package br.ufu.facom.network.translator;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFSwitch;

import org.openflow.protocol.OFMessage;



public interface AppListener {

	/*****************************************************************************************************************
	 * Recebe um Evento do OpenFLOW e envia esta para o SLEE --->JSLEE (JAIN Service Logic Execution Environment)
	 * 
	 * @param sw
	 * @param msg
	 * @param cntx
	 * 
	 * Adapta��o do c�digo :  GIOVANI FRANCISCO DE SANT`ANNA 
	 * 
	 *****************************************************************************************************************
	 */
	public void sendEvent(Long ioFSwitch, OFMessage ofMessage, FloodlightContext floodlightContext);
	
	
}

