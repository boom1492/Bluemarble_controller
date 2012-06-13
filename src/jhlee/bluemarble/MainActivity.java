package jhlee.bluemarble;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	private final static int REQUEST_CODE = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText text_id = (EditText)findViewById(R.id.text_ID);
        Button but_id = (Button)findViewById(R.id.button_startID);
        but_id.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, JoinActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("mCode", text_id.getText().toString());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}); 
        Button but_qr = (Button)findViewById(R.id.button_startQR);
        but_qr.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				startActivityForResult(intent, REQUEST_CODE);
			}
		});
    }
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_CODE){
			if(resultCode == RESULT_OK){
				Uri uri = Uri.parse(data.getStringExtra("SCAN_RESULT"));
				
				String mCode = uri.getQueryParameter("sessionId");

				Intent intent = new Intent(MainActivity.this, JoinActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("mCode", mCode);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			else if(resultCode == RESULT_CANCELED){
				// QR코드 스캐너에서 취소할 경우 핸들링
			}
		}

	}
}