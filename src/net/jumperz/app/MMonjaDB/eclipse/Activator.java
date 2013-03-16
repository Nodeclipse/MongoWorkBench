package net.jumperz.app.MMonjaDB.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

import net.jumperz.app.MMonjaDB.eclipse.action.MSshConnectAction;
import net.jumperz.app.MMonjaDB.eclipse.pref.MPrefManager;
import net.jumperz.app.MMonjaDBCore.MAbstractLogAgent;
import net.jumperz.app.MMonjaDBCore.MConstants;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.MStdoutView;
import net.jumperz.app.MMonjaDBCore.action.MActionManager;
import net.jumperz.app.MMonjaDBCore.event.MEventManager;
import net.jumperz.util.MLogServer;
import net.jumperz.util.MProperties;
import net.jumperz.util.MStreamUtil;
import net.jumperz.util.MSystemUtil;
import net.jumperz.util.MThreadPool;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements MConstants {
	public static final String PLUGIN_ID = "MonjaDB"; //$NON-NLS-1$

	private static Activator plugin;

	private File configFile;

	private volatile Shell shell;

	// --------------------------------------------------------------------------------
	public Activator() {
	}

	// --------------------------------------------------------------------------------
	public synchronized void setShell(Shell s) {
		if (shell == null) {
			shell = s;
			MMenuManager.getInstance().initMenus();
		}
	}

	// --------------------------------------------------------------------------------
	public Shell getShell() {
		return shell;
	}

	// --------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new ones
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	// --------------------------------------------------------------------------------
	private void setupConsole() {
		MessageConsole mc = findConsole(CONSOLE_NAME);
		MessageConsoleStream out = mc.newMessageStream();
		PrintStream ps = new PrintStream(out);
		MAbstractLogAgent.enabled = true;
		MLogServer.getInstance().setSimpleOut(ps);
	}

	// --------------------------------------------------------------------------------
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		setupConsole();
		loadConfig();
		MPrefManager.getInstance().init();

		MLogServer.getInstance().addIgnoredClassName("MAbstractView");
		MEventManager.getInstance().register2(MDataManager.getInstance());
		MEventManager.getInstance().register2(new MStdoutView());
		MEventManager.getInstance().register2(new MAuthManager());
		MThreadPool threadPool = MDataManager.getInstance().getThreadPool();
		// threadPool.addCommand( new MStdinView() );
		MActionManager.getInstance().addAction("^mj connect ssh.*", MSshConnectAction.class);
	}

	// --------------------------------------------------------------------------------
	public void loadConfig() throws IOException {
		Location location = Platform.getConfigurationLocation();
		if (location != null) {
			URL configURL = location.getURL();
			if (configURL != null && configURL.getProtocol().startsWith("file")) {
				File platformDir = new File(configURL.getFile(), Activator.PLUGIN_ID);
				MSystemUtil.createDir(platformDir.getAbsolutePath());
				String configFileName = platformDir.getAbsolutePath() + "/" + DEFAULT_CONFIG_FILE_NAME;
				loadConfig(configFileName);
			}
		} else {
			loadConfig("_dummy_not_exist_");
		}
	}

	// --------------------------------------------------------------------------------
	private void loadConfig(String configFileName) throws IOException {
		// System.out.println( configFileName );
		MProperties prop = new MProperties();
		configFile = new File(configFileName);
		InputStream in = null;
		if (configFile.exists() && configFile.isFile()) {
			in = new FileInputStream(configFile);
		} else {
			in = MStreamUtil.getResourceStream("net/jumperz/app/MMonjaDB/eclipse/resources/" + DEFAULT_CONFIG_FILE_NAME);
		}
		prop.load(in);
		in.close();

		MDataManager.getInstance().setProp(prop);
	}

	// --------------------------------------------------------------------------------
	private void saveConfig() throws IOException {
		OutputStream out = new FileOutputStream(configFile);
		try {
			MDataManager.getInstance().getProp().store(out);
		} finally {
			out.close();
		}
	}

	// --------------------------------------------------------------------------------
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		saveConfig();
		super.stop(context);
	}

	// --------------------------------------------------------------------------------
	public static Activator getDefault() {
		return plugin;
	}
	// --------------------------------------------------------------------------------
}
