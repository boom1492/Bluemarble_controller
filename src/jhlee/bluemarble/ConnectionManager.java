package jhlee.bluemarble;

import java.net.URI;

import org.json.JSONObject;

import android.util.Log;
import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.JunctionMaker;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

public class ConnectionManager {
		
		private static ConnectionManager con;
		private static final String mHost = Global.mHost;
		private static final SwitchboardConfig mSbConfig = new XMPPSwitchboardConfig(mHost);
		private static final JunctionMaker mMaker = JunctionMaker.getInstance(mSbConfig);
		
		private static Junction jx;
		public static JunctionActor actor;
		private String mSessionID = "test";
		
		private ConnectionManager(){
			actor = new MyActor();
			try {
				jx = mMaker.newJunction(URI.create("http://"+mHost+"/"+mSessionID), actor);
				Log.d("debug", "연결 성공");
			} catch (JunctionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("debug", "연결 실패");
			}
		}
		
		public static ConnectionManager getInstance(){
			if(con==null){
				con = new ConnectionManager();
			}
			return con;
		}
		
		public void sendMessageToSession(JSONObject msg){
			actor.sendMessageToSession(msg);
		}
		
		public class MyActor extends JunctionActor{

			@Override
			public void onMessageReceived(MessageHeader header, JSONObject message) {
				// TODO Auto-generated method stub
				
			}
			
		}
}
