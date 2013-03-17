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
 *  
 */
package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;

import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;
import net.jumperz.app.MMonjaDBCore.action.mj.*;

public class MActionManager extends MAbstractLogAgent implements MConstants, MSubject2 {
	private static final MActionManager instance = new MActionManager();

	private Map actionMap;
	private MThreadPool threadPool = MDataManager.getInstance().getActionThreadPool();
	private MSubject2 subject2 = new MSubject2Impl();

	public static MActionManager getInstance() {
		return instance;
	}

	public void addAction(String regex, Class clazz) {
		actionMap.put(regex, clazz);
	}

	private MActionManager() {
		actionMap = new LinkedHashMap();
		actionMap.put("^connect.*", MConnectAction.class);
		actionMap.put("^mj disconnect", MDisconnectAction.class);
		actionMap.put("^show\\s+collections", MShowCollectionAction.class);
		actionMap.put("^show\\s+dbs", MShowDBAction.class);
		actionMap.put("^use\\s+.*", MUseAction.class);
		actionMap.put("^db\\.[^\\(]+\\.find\\(.*", MFindAction.class);
		actionMap.put("^mj show all db stats$", MShowAllDbStatsAction.class);
		actionMap.put("^mj show all collection stats$", MShowAllCollectionStatsAction.class);
		actionMap.put("^mj sort .*", MSortAction.class);
		actionMap.put("^db\\.[^\\(]+\\.update\\(.*", MUpdateAction.class);
		actionMap.put("^db\\.[^\\(]+\\.save\\(.*", MSaveAction.class);
		actionMap.put("^db\\.[^\\(]+\\.insert\\(.*", MInsertAction.class);
		actionMap.put("^db\\.[^\\(]+\\.remove\\(.*", MRemoveAction.class);
		actionMap.put("^mj edit field .*", MEditFieldAction.class);
		actionMap.put("^mj edit .*", MEditAction.class);
		actionMap.put("^mj update int field.*", MUpdateIntFieldAction.class);
		actionMap.put("^mj prev items$", MPrevItemsAction.class);
		actionMap.put("^mj next items$", MNextItemsAction.class);
		actionMap.put("^mj copy$", MCopyAction.class);
		actionMap.put("^mj paste$", MPasteAction.class);
		actionMap.put("^mj remove$", MMjRemoveAction.class);
		// actionMap.put( "^mj connect ssh.*", MSshConnectAction.class );
	}

	public MAction getAction(String actionStr) {
		if (actionStr == null || actionStr.length() < 1) {
			return null;
		}

		// remove line breaks
		actionStr = actionStr.replaceAll("(\\r|\\n)", "");
		actionStr = actionStr.replaceAll("\\t+", " ");
		
		Iterator p = actionMap.keySet().iterator();
		while (p.hasNext()) {
			String patternStr = (String) p.next();
			if (actionStr.matches(patternStr)) {
				Class clazz = (Class) actionMap.get(patternStr);
				try {
					MAction maction = (MAction) clazz.newInstance();
					maction.setCmd( actionStr );
					maction.parse( actionStr );
					return maction;
				} catch (Exception e) {
					MEventManager.getInstance().fireErrorEvent(e);
					return null;
				}
			}
		}
		return null;
	}

	public void submitForExecution(MAction action, MInputView view){
		action.setOriginView(view);
		threadPool.addCommand(action);
	}
	
	public MAction executeAction(String actionStr, MInputView view) {
		if (actionStr == null || actionStr.length() < 1) {
			return null;
		}

		// remove line breaks
		actionStr = actionStr.replaceAll("(\\r|\\n)", "");
		actionStr = actionStr.replaceAll("\\t+", " ");

		MAction action = getAction(actionStr);
		if (action == null) {
			MEventManager.getInstance().fireErrorEvent( new MParseException("Action not found : " + actionStr) );
			return null;
		}

		action.setOriginView(view);
		action.setCmd(actionStr);

		if (!action.parse(actionStr)) {
			debug(action);
			MEventManager.getInstance().fireErrorEvent(new MParseException("Parse Error : " + actionStr));
			return null;
		}

		threadPool.addCommand(action);
		return action;
	}

	public MAction executeAction(String actionStr) {
		return executeAction(actionStr, null);
	}

	public void notify2(Object event, Object source) {
		subject2.notify2(event, source);
	}

	public void register2(MObserver2 observer) {
		subject2.register2(observer);
	}

	public void removeObservers2() {
		subject2.removeObservers2();
	}

	public void removeObserver2(MObserver2 observer) {
		subject2.removeObserver2(observer);
	}
}