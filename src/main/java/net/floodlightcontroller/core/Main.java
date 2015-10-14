package net.floodlightcontroller.core;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import net.floodlightcontroller.core.internal.CmdLineSettings;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.FloodlightModuleLoader;
import net.floodlightcontroller.core.module.IFloodlightModuleContext;
import net.floodlightcontroller.restserver.IRestApiService;

/*******************************************************************************************************
 * Host for the Floodlight main method
 * @author alexreimers
 * 
 * Adapta��o do c�digo :  GIOVANI FRANCISCO DE SANT`ANNA - Doutorando em Ci�ncia da Computa��o - UFU
 * *****************************************************************************************************
 */
public class Main {

	/*******************************************************************************************************
	 * Met�do Principal para carregar configura��es e m�dulos - Floodlight
	 * 
	 * Adapta��o do c�digo :  GIOVANI FRANCISCO DE SANT`ANNA 
	 * *****************************************************************************************************
	 */
    public static void main(String[] args) throws FloodlightModuleException {
        // Setup logger
        System.setProperty("org.restlet.engine.loggerFacadeClass", 
                "org.restlet.ext.slf4j.Slf4jLoggerFacade");
        
        CmdLineSettings settings = new CmdLineSettings();
        CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            System.exit(1);
        }
        
    	/*******************************************************************************************************
    	 * Carregando os m�dulos - Floodlight
    	 * 
    	 * Adapta��o do c�digo :  GIOVANI FRANCISCO DE SANT`ANNA 
    	 * *****************************************************************************************************
    	 */
        // Load modules
        FloodlightModuleLoader fml = new FloodlightModuleLoader();
        IFloodlightModuleContext moduleContext = fml.loadModulesFromConfig(settings.getModuleFile());
        // Run REST server
        IRestApiService restApi = moduleContext.getServiceImpl(IRestApiService.class);
        restApi.run();
    	/*******************************************************************************************************
    	 * Carregando o M�dulo Principal do Floodlight
    	 * 
    	 * Adapta��o do c�digo :  GIOVANI FRANCISCO DE SANT`ANNA 
    	 * *****************************************************************************************************
    	 */
        // Run the main floodlight module
        IFloodlightProviderService controller =
                moduleContext.getServiceImpl(IFloodlightProviderService.class);
        // This call blocks, it has to be the last line in the main
        controller.run();
    }
}
