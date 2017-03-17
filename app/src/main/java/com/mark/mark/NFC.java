package com.mark.mark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NFC extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    TextView textViewInfo;
    SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static String lec_location;
    public static String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        textViewInfo = (TextView)findViewById(R.id.textInfo);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, MainActivity.MODE_PRIVATE);
        lec_location = getIntent().getStringExtra("lec_location");
        subject = getIntent().getStringExtra("subject");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this,"NFC NOT supported on this devices!", Toast.LENGTH_LONG).show();
            finish();
        }else if(!nfcAdapter.isEnabled()){
            Toast.makeText(this,"NFC NOT Enabled!",Toast.LENGTH_LONG).show();
//            finish();
        }
    }
    // Triggers when Scan NFC Button clicked
    public void scanFingerprint(View arg0) {
        Intent intent =  new Intent(NFC.this, ScanFPActivity.class);
        intent.putExtra("subject", subject);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Toast.makeText(this,
                    "onResume() - ACTION_TAG_DISCOVERED",
                    Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag == null){
                textViewInfo.setText("tag == null");
            }else{
                String tagInfo = tag.toString() + "\n";

                tagInfo += "\nTag Id: \n";
                byte[] tagId = tag.getId();
                tagInfo += "length = " + tagId.length +"\n";
                for(int i=0; i<tagId.length; i++){
                    tagInfo += Integer.toHexString(tagId[i] & 0xFF) + " ";
                }
                tagInfo += "\n";

                String[] techList = tag.getTechList();
                tagInfo += "\nTech List\n";
                tagInfo += "length = " + techList.length +"\n";
                for(int i=0; i<techList.length; i++){
                    tagInfo += techList[i] + "\n ";
                }
                textViewInfo.setText(tagInfo);
            }
        }else{
            String tagUID = "C09705f7";
            String location = "61";
            long time= System.currentTimeMillis();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("NFC_Location", location);
            editor.putString("NFC_TimeMills", time+"");
            editor.putString("NFC_UID", tagUID);

            Toast.makeText(this,"NFC UID: " + tagUID, Toast.LENGTH_LONG).show();

            if(lec_location.equals(location))
                Toast.makeText(this,"You are in the right location: " + lec_location, Toast.LENGTH_LONG).show();
            else{
                Toast.makeText(this,"Oops! You are in the wrong location. \nPlease goto -" + lec_location, Toast.LENGTH_LONG).show();
            }
        }

    }
}
