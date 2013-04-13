/* 
 *  MongoWorkBench is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  MongoWorkBench is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *    
 *  https://github.com/aw20/MongoWorkBench
 *  Original fork: https://github.com/Kanatoko/MonjaDB
 */
package net.jumperz.app.MMonjaDB.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jumperz.app.MMonjaDB.eclipse.pref.MPrefManager;
import net.jumperz.app.MMonjaDBCore.MConstants;

import org.aw20.io.StreamUtil;
import org.aw20.util.FileUtil;
import org.aw20.util.StringUtil;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.mongodb.MongoClient;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements MConstants {
	public static final String PLUGIN_ID = "MongoWorkBench"; //$NON-NLS-1$

	private static Activator plugin;
	private File configFile, commandFile, serverListFile;
	private volatile Shell shell;

	public synchronized void setShell(Shell s) {
		if (shell == null) {
			shell = s;
			MMenuManager.getInstance().initMenus();
		}
	}

	public void showView( String viewname ){
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.showView( viewname, null, IWorkbenchPage.VIEW_VISIBLE );
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	public Shell getShell() {
		return shell;
	}

	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		loadConfig();
		MPrefManager.getInstance().init();
	}

	public void loadConfig() throws IOException {
		Location location = Platform.getConfigurationLocation();
		if (location != null) {
			URL configURL = location.getURL();
			if (configURL != null && configURL.getProtocol().startsWith("file")) {
				File platformDir = new File(configURL.getFile(), Activator.PLUGIN_ID);
				createDir(platformDir.getAbsolutePath());
				String configFileName = platformDir.getAbsolutePath() + "/" + DEFAULT_CONFIG_FILE_NAME;
				
				commandFile	= new File( platformDir.getAbsolutePath(), "command.txt" );
				serverListFile	= new File( platformDir.getAbsolutePath(), "serverlist.bin" );
				
				loadConfig(configFileName);
			}
		} else {
			loadConfig("_dummy_not_exist_");
		}
	}

	private void loadConfig(String configFileName) throws IOException {
		configFile = new File(configFileName);
		InputStream in = null;
		
		try{
			
			if (configFile.exists() && configFile.isFile()) {
				in = new FileInputStream(configFile);
			} else {
				in = StreamUtil.getResourceStream("net/jumperz/app/MMonjaDB/eclipse/resources/" + DEFAULT_CONFIG_FILE_NAME);
			}
		
		}finally{
			if ( in != null )
				in.close();
		}
		
	}

	public void saveConfig() throws IOException {
		OutputStream out = new FileOutputStream(configFile);
		try {

		} finally {
			out.close();
		}
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		saveConfig();
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public void saveWorkBench(int type, String text) {
		try {
			File cmdFile = new File( commandFile.getParent(), type + commandFile.getName() );
			FileUtil.writeToFile(cmdFile, text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getWorkBench(int type) {
		try {
			File cmdFile = new File( commandFile.getParent(), type + commandFile.getName() );
			return FileUtil.readToString(cmdFile);
		} catch (IOException e) {
			return "";
		}
	}

	public List<Map> getServerList() {
		List<Map> serverList	= (List<Map>)FileUtil.loadClass(serverListFile);
		if ( serverList == null )
			return new ArrayList<Map>();
		else
			return serverList;
	}

	public void saveServerList(List<Map> serverList){
		FileUtil.saveClass(serverListFile, serverList);
	}

	public Map<String, Object> getServerMap(String sName) {
		Iterator<Map>	it	= getServerList().iterator();
		while ( it.hasNext() ){
			Map	m = it.next();
			if ( m.get("name").equals(sName) )
				return m;
		}
		return null;
	}
	
	public List<Map> removeServerMap(String sName) {
		List<Map> list = getServerList();
		Iterator<Map>	it	= list.iterator();
		while ( it.hasNext() ){
			Map	m = it.next();
			if ( m.get("name").equals(sName) ){
				it.remove();
				break;
			}
		}
		return list;
	}

	public MongoClient getMongoClient(String sName) throws UnknownHostException{
		Map<String, Object>	props	= getServerMap( sName );
		if ( props == null )
			return null;
		
		return new MongoClient( (String)props.get("host"), StringUtil.toInteger(props.get("port"), 27017) );
	}
	
	
	private String createDir(String dirName) throws IOException {
		File dir = new File(dirName);
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IOException("Couldn't make directory: " + dir.getCanonicalPath());
			}
		} else {
			if (!dir.mkdirs()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {}
				if (!dir.isDirectory()) {
					throw new IOException("Couldn't make directory: " + dir.getCanonicalPath());
				}
			}
		}
		return dir.getCanonicalPath();
	}
}
