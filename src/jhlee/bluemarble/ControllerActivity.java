package jhlee.bluemarble;

import java.net.URI;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.JunctionMaker;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

public class ControllerActivity extends Activity{
	private static final String mHost = "mobilesw.yonsei.ac.kr";
	private static final SwitchboardConfig mSbConfig = new XMPPSwitchboardConfig(mHost);
	private static final JunctionMaker mMaker = JunctionMaker.getInstance(mSbConfig);

	private static final int RED = 0, BLUE = 1, GREEN = 2, ORANGE = 3; 
	private static final int STATE_BUYLAND = 1;
	private static final int STATE_BUILD = 2;
	private static final int STATE_GOLDENKEY = 3;
	private static final int STATE_ALONE = 4;
	private static final int STATE_DICE = 8;
	private static final int STATE_TOLL = 0;

	private static Junction jx;
	private static JunctionActor actor;
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		JSONObject message = new JSONObject();
		try {
			message.put("service", "exit");
			message.put("number", mNumber);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		actor.sendMessageToSession(message);
		actor.leave();
		jx.disconnect();
		super.onDestroy();
	}


	private String actorID;
	private String mNickname;
	private String mCode;
	private int mNumber = -1;
	private String[] mNames = new String[4];
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller_ready);
		Bundle bundle = getIntent().getExtras();
		mNickname = bundle.getString("nickname");
		mCode = bundle.getString("mCode");
		
		MyTask task = new MyTask();
		task.execute(mCode);
		
		Button but_ready = (Button)findViewById(R.id.button_ready);
		but_ready.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				JSONObject message = new JSONObject();
				try {
					message.put("service", "ready");
					message.put("number", mNumber);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				actor.sendMessageToSession(message);
			}
		});
	}
	

	class ControllerActor extends JunctionActor{

		@Override
		public void onMessageReceived(MessageHeader header, JSONObject message) {
			// TODO Auto-generated method stub
			try {
				String service = message.getString("service");
				final int cur_number = message.getInt("number");
				if(service.equals("ackjoin")){
					for(int i=0;i<4;i++){
						mNames[i] = message.getJSONArray("namelist").getString(i);
					}
				}
				if(mNumber==-1){
					if(service.equals("ackjoin")){
						mNumber = message.getInt("number");
						mNickname = message.getString("name");
						
						runOnUiThread(new Runnable() {
							public void run() {
								// TODO Auto-generated method stub

								TextView text_player = (TextView)findViewById(R.id.text_player);
								text_player.setText(mNickname);
								switch(mNumber){
								case RED:
									text_player.setTextColor(Color.RED);
									break;
								case BLUE:
									text_player.setTextColor(Color.BLUE);
									break;
								case GREEN:
									text_player.setTextColor(Color.GREEN);
									break;
								case ORANGE:
									text_player.setTextColor(Color.YELLOW);
									break;
								default:
									break;
								}
							}
						});
					}	
				}
				if((cur_number == mNumber)){
					if(service.equals("turn")){
						int state = message.getInt("state");
						switch(state){
						case STATE_DICE:
							runOnUiThread(new Runnable() {
								
								public void run() {
									// TODO Auto-generated method stub
									setContentView(R.layout.controller_dice);
									Button button_dice = (Button)findViewById(R.id.button_dice);
									button_dice.setOnClickListener(new OnClickListener() {
										
										public void onClick(View v) {
											// TODO Auto-generated method stub
											Random r = new Random();
											int n1, n2;
											n1 = r.nextInt(6)+1;
											n2 = r.nextInt(6)+1;
											JSONObject message = new JSONObject();
											try {
												message.put("service", "action");
												message.put("state", STATE_DICE);
												message.put("n1", n1);
												message.put("n2", n2);
												message.put("number", mNumber);
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											actor.sendMessageToSession(message);
											setContentView(R.layout.controller_gap);
											Toast.makeText(ControllerActivity.this, n1+", " + n2 + "가 나왔습니다.", Toast.LENGTH_SHORT).show();
										}
									});
								}
							});
							
							break;
						case STATE_BUYLAND:
							final String locationName = message.getString("locationName");
							final int locationValue = message.getInt("locationValue");
							final int locationOwner = message.getInt("locationOwner");
							final int playerMoney = message.getInt("playerMoney");
							runOnUiThread(new Runnable() {
								public void run() {
									setContentView(R.layout.controller_buy);
									TextView text_locationName = (TextView)findViewById(R.id.text_location);
									TextView text_information = (TextView)findViewById(R.id.text_locationinfo);
									Button button_buy = (Button)findViewById(R.id.button_buy);
									Button button_skip = (Button)findViewById(R.id.button_skip);
									text_locationName.setText(locationName);
									if(locationOwner==-1){
										text_information.setText("현재 이 땅은 소유주가 없습니다.\n" +
												"땅의 가격은 " + locationValue + "입니다.\n" +
												"구매하시겠습니까?");
									}
									button_buy.setOnClickListener(new OnClickListener() {
										public void onClick(View v) {
											// TODO Auto-generated method stub
											if(playerMoney>=locationValue){
												JSONObject message = new JSONObject();
												try {
													message.put("service", "action");
													message.put("state", STATE_BUYLAND);
													message.put("agree", 1);
													message.put("number", mNumber);
												} catch (JSONException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
												actor.sendMessageToSession(message);
												setContentView(R.layout.controller_gap);
											}
										}
									});
									button_skip.setOnClickListener(new OnClickListener() {
										
										public void onClick(View v) {
											// TODO Auto-generated method stub
											JSONObject message = new JSONObject();
											try {
												message.put("service", "action");
												message.put("state", STATE_BUYLAND);
												message.put("agree", 0);
												message.put("number", mNumber);
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											actor.sendMessageToSession(message);
											setContentView(R.layout.controller_gap);
										}
									});
								}
							});
							break;
						case STATE_BUILD:

							final int state_villa = message.getInt("state_villa");
							final int state_building = message.getInt("state_building");
							final int state_hotel = message.getInt("state_hotel");
							final int value_villa = message.getInt("value_villa");
							final int value_building = message.getInt("value_building");
							final int value_hotel = message.getInt("value_hotel");
							final int toll_villa = message.getInt("toll_villa");
							final int toll_building = message.getInt("toll_building");
							final int toll_hotel = message.getInt("toll_hotel");
							final int money = message.getInt("playerMoney");
							
							runOnUiThread(new Runnable() {
								public void run() {
									setContentView(R.layout.controller_build);
									
									CheckBox check_villa = (CheckBox)findViewById(R.id.checkBox_villa);
									CheckBox check_building = (CheckBox)findViewById(R.id.checkBox_building);
									CheckBox check_hotel = (CheckBox)findViewById(R.id.checkBox_hotel);
									TextView text_value_villa = (TextView)findViewById(R.id.text_valuevilla);
									TextView text_value_building = (TextView)findViewById(R.id.text_valuebuilding);
									TextView text_value_hotel = (TextView)findViewById(R.id.text_valuehotel);
									TextView text_toll_villa = (TextView)findViewById(R.id.text_feevilla);
									TextView text_toll_building = (TextView)findViewById(R.id.text_feebuilding);
									TextView text_toll_hotel = (TextView)findViewById(R.id.text_feehotel);
									Button button_skip = (Button)findViewById(R.id.button_skipbuild);
									button_skip.setOnClickListener(new OnClickListener() {
										
										public void onClick(View v) {
											// TODO Auto-generated method stub
											JSONObject message = new JSONObject();
											try {
												message.put("service", "action");
												message.put("number", mNumber);
												message.put("state", STATE_BUILD);
												message.put("build", 3);
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											actor.sendMessageToSession(message);
											setContentView(R.layout.controller_gap);
										}
									});
									if(state_villa==1){
										check_villa.setChecked(true);
										check_villa.setClickable(false);
									}else{
										check_villa.setChecked(false);
									}
									if(state_building==1){
										check_building.setChecked(true);
										check_building.setClickable(false);
									}else{
										check_building.setChecked(false);
									}
									if(state_hotel==1){
										check_hotel.setChecked(true);
										check_hotel.setClickable(false);
									}else{
										check_hotel.setChecked(false);
									}
									if(money<value_villa){
										check_villa.setEnabled(false);
									}
									if(money<value_building){
										check_building.setEnabled(false);
									}
									if(money<value_hotel){
										check_hotel.setEnabled(false);
									}
									text_value_villa.setText(Integer.toString(value_villa));
									text_value_building.setText(Integer.toString(value_building));
									text_value_hotel.setText(Integer.toString(value_hotel));
									text_toll_villa.setText(Integer.toString(toll_villa));
									text_toll_building.setText(Integer.toString(toll_building));
									text_toll_hotel.setText(Integer.toString(toll_hotel));
					
									OnCheckedChangeListener listener = new OnCheckedChangeListener() {
										
										public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
											// TODO Auto-generated method stub
											if(buttonView.getId()==R.id.checkBox_villa){
												if(isChecked){
													JSONObject message = new JSONObject();
													try {
														message.put("service", "action");
														message.put("number", mNumber);
														message.put("state", STATE_BUILD);
														message.put("build", 0);
													} catch (JSONException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													actor.sendMessageToSession(message);
													setContentView(R.layout.controller_gap);
												}
											} else if(buttonView.getId()==R.id.checkBox_building){
												if(isChecked){
													JSONObject message = new JSONObject();
													try {
														message.put("service", "action");
														message.put("number", mNumber);
														message.put("state", STATE_BUILD);
														message.put("build", 1);
													} catch (JSONException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													actor.sendMessageToSession(message);
													setContentView(R.layout.controller_gap);
												}
											} else{
												if(isChecked){
													JSONObject message = new JSONObject();
													try {
														message.put("service", "action");
														message.put("number", mNumber);
														message.put("state", STATE_BUILD);
														message.put("build", 2);
													} catch (JSONException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													actor.sendMessageToSession(message);
													setContentView(R.layout.controller_gap);
												}
											}
										}
									};
									check_villa.setOnCheckedChangeListener(listener);
									check_building.setOnCheckedChangeListener(listener);
									check_hotel.setOnCheckedChangeListener(listener);
								}
							});
							
							break;
							
						case STATE_TOLL:
							final int pay = message.getInt("toll");
							final String payer = message.getString("recevier_number");
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(ControllerActivity.this, payer+"님에게 "+pay+"원을 지불했습니다.", Toast.LENGTH_LONG).show();
									setContentView(R.layout.controller_gap);
								}
							});
							
							break;
						default:
							break;
						}
					}
				}
				else{
					if(service.equals("turn")){
						runOnUiThread(new Runnable() {
							public void run() {
								
								setContentView(R.layout.controller_wait);
								TextView text_playerturninfo = (TextView)findViewById(R.id.text_playerturninfo);
								String str = "현재 ";
								str += mNames[cur_number];
								str += "님의 차례입니다.";
								text_playerturninfo.setText(str);
								switch(cur_number){
								case RED:
									text_playerturninfo.setTextColor(Color.RED);
									break;
								case BLUE:
									text_playerturninfo.setTextColor(Color.BLUE);
									break;
								case GREEN:
									text_playerturninfo.setTextColor(Color.GREEN);
									break;
								case ORANGE:
									text_playerturninfo.setTextColor(Color.YELLOW);
									break;
								default:
									break;
								}
							}
						});
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}


	class MyTask extends AsyncTask<String, Void, Boolean>{
		private ProgressDialog mDialog;
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				actor = new ControllerActor();
				actorID = actor.getActorID();
				jx = mMaker.newJunction(URI.create("http://"+mHost+"/"+params[0]), actor);
			} catch (JunctionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			JSONObject msg = new JSONObject();
			try {
				msg.put("service", "join");
				msg.put("name", mNickname);
				msg.put("actorID", actorID);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			actor.sendMessageToSession(msg);
			return true;
		}
		@Override
		protected void onPreExecute() {
			if (mDialog == null) {
				mDialog = new ProgressDialog(ControllerActivity.this);
				mDialog.setMessage("게임에 접속중입니다.");
				mDialog.setIndeterminate(true);
				mDialog.setCancelable(true);
				mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface arg0) {
						//actor.leave();
					}
				});
				mDialog.show();	
			}
		}

		protected void onPostExecute(Boolean bool) {
			mDialog.hide();
			if(bool){
				Toast.makeText(ControllerActivity.this, "접속에 성공하였습니다.", Toast.LENGTH_SHORT).show();
				runOnUiThread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub

						TextView text_player = (TextView)findViewById(R.id.text_player);
						text_player.setText(mNickname);
					}
				});
			} else{
				Toast.makeText(ControllerActivity.this, "접속에 실패하였습니다.", Toast.LENGTH_SHORT).show();
			}
		};
	}


}
