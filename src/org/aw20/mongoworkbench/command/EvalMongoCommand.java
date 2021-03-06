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
 */
package org.aw20.mongoworkbench.command;

import java.util.List;
import java.util.Map;

import org.aw20.mongoworkbench.MongoFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class EvalMongoCommand extends MongoCommand {
	
	private Map<String,Object>	map;
	private String eval;
	
	public EvalMongoCommand(String text) {
		eval = text;
	}

	@Override
	public void execute() throws Exception {
		MongoClient mdb = MongoFactory.getInst().getMongo( sName );
		
		if ( mdb == null )
			throw new Exception("no server selected");

		String db;
		List<String> listDBs = mdb.getDatabaseNames();
		if ( listDBs.size() > 0 )
			db	= listDBs.get(0);
		else
			db	= "admin";
		
		DB dbM	= mdb.getDB( db );

		Object obj = dbM.eval( eval, (Object[])null );
		if ( obj instanceof Map ){
			map	= (Map)obj;
		}
		
		setMessage("eval() ran");
	}

	public Map<String,Object>	getStatus(){
		return map;
	}
	
	@Override
	public String getCommandString() {
		return "aw20.eval()";
	}
}