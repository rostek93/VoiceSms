package com.example.kamil.voicesms;

//package com.learn2crack.speech;

import java.util.ArrayList;

import android.content.ComponentName;
import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.Bundle;
        import android.app.Activity;
        import android.app.Dialog;
        import android.content.Context;
        import android.content.Intent;
        import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 1234;
    ImageButton Start;
    TextView Speech;
    Button Clear;
    Dialog match_text_dialog;
    ListView textlist;
    ArrayList<String> matches_text;
    String allText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Start = (ImageButton)findViewById(R.id.start_reg);
        Speech = (TextView)findViewById(R.id.speech);
        Clear = (Button)findViewById(R.id.clearButton);

        // Rozpoczyna nasłuchiwanie, a na koncu ptrzetwarza na tekst
        Start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
            //    if(true){

                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                 //   intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
                }}

        });

        Clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                allText="";
                Speech.setText("Tekst: \n" +allText);
            }
        });
    }



    public  boolean isConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!=null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            allText += interpunctionAlgorithm(matches_text.get(0));

            Speech.setText("Tekst: \n" +allText);

            /*
            match_text_dialog = new Dialog(MainActivity.this);
            match_text_dialog.setContentView(R.layout.dialog_matches_frag);
            match_text_dialog.setTitle("Select Matching Text");
            textlist = (ListView)match_text_dialog.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter =    new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matches_text);
            textlist.setAdapter(adapter);
            textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Speech.setText("You have said " +matches_text.get(position));
                    match_text_dialog.hide();
                }
            });
            match_text_dialog.show();
*/
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String interpunctionAlgorithm(String text) {


        String[] parts = text.split(" ");
        text = "";

        for (int i=0; i<parts.length; i++) {

            if(parts[i].equals("kropka")) {
                parts[i] = ".";
                if (i+1 < parts.length)
                    if (!parts[i+1].equals("przecinek") && !parts[i+1].equals("znak") && !parts[i+1].equals("wykrzyknik"))
                        parts[i+1] = Character.toUpperCase(parts[i+1].charAt(0)) + (parts[i+1].length() > 1 ? parts[i+1].substring(1) : "");
            }

            else if (parts[i].equals("przecinek")) {
                parts[i] = ",";
            }

            else if (parts[i].equals("znak")) {
                if (i+1 < parts.length)
                    if (parts[i+1].equals("zapytania")) {
                        parts[i] = "?";
                        parts[i+1] = "";
                    }
                if (i+2 < parts.length)
                    if (!parts[i+2].equals("kropka") && !parts[i+2].equals("przecinek") && !parts[i+2].equals("wykrzyknik"))
                        parts[i+2] = Character.toUpperCase(parts[i+2].charAt(0)) + (parts[i+4].length() > 1 ? parts[i+2].substring(1) : "");
            }

            else if (parts[i].equals("wykrzyknik")) {
                parts[i] = "!";
                if (i+1 < parts.length)
                    if (!parts[i+1].equals("kropka") && !parts[i+1].equals("znak") && !parts[i+1].equals("przecinek"))
                        parts[i+1] = Character.toUpperCase(parts[i+1].charAt(0)) + (parts[i+1].length() > 1 ? parts[i+1].substring(1) : "");
            }

        }

        for (int i=0; i<parts.length; i++) {
            if (i == 0 && !parts[i].equals("?") && !parts[i].equals(".") && !parts[i].equals("!") && !parts[i].equals("!"))
                text += Character.toUpperCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
            else if (parts[i] == "." || parts[i] == "?" || parts[i] == "!" || parts[i] == ",")
                text += parts[i];
            else if (parts[i] != "")
                text += " " + parts[i];
        }

        return text;
    }

}
