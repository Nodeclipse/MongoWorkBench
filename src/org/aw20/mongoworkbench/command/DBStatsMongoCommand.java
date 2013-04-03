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
package org.aw20.mongoworkbench.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aw20.mongoworkbench.MongoFactory;

import com.mongodb.CommandResult;
import com.mongodb.MongoClient;

public class DBStatsMongoCommand extends ShowDbsMongoCommand {

	private List<Map>	statsListMap;
	
	@Override
	public void execute() throws Exception {
		MongoClient mdb = MongoFactory.getInst().getMongo( sName );
		
		if ( mdb == null )
			throw new Exception("no server selected");

		setDBNames(mdb);
		statsListMap	= new ArrayList<Map>();
		
		Iterator<String> it = dbNames.iterator();
		while ( it.hasNext() ){
			String db = it.next();
			
			CommandResult cmdr = mdb.getDB(db).getStats();
			statsListMap.add( cmdr.toMap() );
		}
		
		setCommandStr("Retrived Database Stats; db=" + statsListMap.size() );
	}

	public List<Map> getStatsListMap(){
		return statsListMap;
	}
	
}
