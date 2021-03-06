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
 *  
 *  April 2013
 */
package org.aw20.mongoworkbench.command;

import org.aw20.mongoworkbench.MongoFactory;
import org.bson.types.Code;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class SystemJavaScriptReadCommand extends MongoCommand {

	private String jsName = null, jsCode = null;
	
	public SystemJavaScriptReadCommand(String jsName) {
		this.jsName = jsName;
	}

	@Override
	public void execute() throws Exception {

		MongoClient mdb = MongoFactory.getInst().getMongo( sName );
		
		if ( mdb == null )
			throw new Exception("no server selected");
		
		if ( sDb == null )
			throw new Exception("no database selected");
		
		MongoFactory.getInst().setActiveDB(sDb);
		DB db	= mdb.getDB(sDb);

		DBCollection coll	= db.getCollection("system.js");
		DBObject dbo = coll.findOne( new BasicDBObject("_id", jsName) );
		
		if ( dbo.containsField("value") ){
			jsCode	= ((Code)dbo.get("value")).getCode();
		}
		
		setMessage("System JavaScript loaded=" + jsName);
	}

	public String getCode(){
		return jsCode;
	}
	
	public String getJSName(){
		return jsName;
	}
	
	public String getCommandString() {
		return "aw20.systemjs.read(\"" + jsName + "\")";
	}
}
