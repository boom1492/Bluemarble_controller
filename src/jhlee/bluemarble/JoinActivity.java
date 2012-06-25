package jhlee.bluemarble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class JoinActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joinplayer);
   
        Bundle bundle = getIntent().getExtras();
        final String mCode = bundle.getString("mCode");
        Button but_nickname = (Button)findViewById(R.id.button_nickname);
        final EditText text_nickname = (EditText)findViewById(R.id.text_nickname);
        but_nickname.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String nickname = text_nickname.getText().toString();
				Intent intent = new Intent(JoinActivity.this, ControllerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("mCode", mCode);
				bundle.putString("nickname", nickname);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
        
    }
}
