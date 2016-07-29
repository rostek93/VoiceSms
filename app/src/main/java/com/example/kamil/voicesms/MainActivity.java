package com.example.kamil.voicesms;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

enum FieldEnum { contact, message }

public class MainActivity extends Activity {
    private static final String TAG = "VoiceSMS";

    private ImageButton startButton;
    private EditText messageField;
    private EditText contactField;
    private TextView messageTextView;
    private TextView contactTextView;
    private FieldEnum fieldEnum = FieldEnum.message;
    private boolean sendMessage = false;
    private ImageView iconImageView;
    private SpeechRecognizer mSpeechRecognizer = null;

    public static String messageText = "";
    private String contactText = "";
    private boolean correctContact = false;


    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (ImageButton) findViewById(R.id.startButton);
        Button clearButton = (Button) findViewById(R.id.clearButton);
        Button exitButton = (Button) findViewById(R.id.exitButton);
        messageField = (EditText) findViewById(R.id.messageField);
        contactField = (EditText) findViewById(R.id.contactField);
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        contactTextView = (TextView) findViewById(R.id.contactTextView);
        messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
        messageField.setSelection(messageField.length());
        messageField.requestFocus();
        iconImageView = (ImageView) findViewById(R.id.imageView);
        iconImageView.setImageResource(R.drawable.redicon);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechRecognizer == null) {
                    if (isConnected()) {
                        startVoiceRead();
                       // startButton.setImageResource(R.drawable.soundoff);
                        Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_LONG).show();
                } else {
                    stopVoiceRead();
                    //startButton.setImageResource(R.drawable.soundon);
                    Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_LONG).show();
                }
            }
        });

         clearButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 messageText = "";
                 messageField.setText(messageText);
             }
         });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVoiceRead();
                System.exit(0);
            }
        });
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net != null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void startVoiceRead() {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        startButton.setImageResource(R.drawable.soundoff);
      //  speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
      //  speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS , 3000);
      //  speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS , 5000);
        getSpeechRecognizer().startListening(speechIntent);
    }

    public void stopVoiceRead() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
            iconImageView.setImageResource(R.drawable.redicon);
            startButton.setImageResource(R.drawable.soundon);
        }
    }

    private SpeechRecognizer getSpeechRecognizer() {
        if (mSpeechRecognizer == null) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "onReadyForSpeech");
                    iconImageView.setImageResource(R.drawable.greenicon);
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "onBeginningOfSpeech");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.d(TAG, "onBufferReceived");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "onEndOfSpeech");
                    iconImageView.setImageResource(R.drawable.redicon);
                }

                @Override
                public void onError(int error) {
                    iconImageView.setImageResource(R.drawable.redicon);
                    String message;
                    boolean restart = true;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "Audio recording error";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "Client side error";
                            restart = false;
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "Insufficient permissions";
                            restart = false;
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "Network error";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "Network timeout";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "No match";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RecognitionService busy";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "error from server";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "No speech input";
                            break;
                        default:
                            message = "Not recognised";
                            break;
                    }
                    // mTextView.append("onError code:" + error + " message: " + message);
                    Log.d(TAG, "onError:" + message);
                    if (restart) {
                        getSpeechRecognizer().cancel();
                        startVoiceRead();
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.d(TAG, "onEvent");
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    Log.d(TAG, "onPartialResults");

                    ArrayList<String> text = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String temp;
                    if (text.size() > 0) {
                        counter++;
                       // temp = String.valueOf(counter);
                        temp = text.get(0);
                      //  temp.toLowerCase();
                      //  temp += "\n";
                        if (!messageText.equals(temp)) {
                            messageText = temp;
                            //messageText = (temp + messageText);
                            messageField.append(counter + temp + "\n");
                            // messageField.setText(String.valueOf(counter) + messageText);
                            //  messageText = "";
                           // processingText(temp);
                        }
                    }

                }

                @Override
                public void onResults(Bundle results) {
                    Log.d(TAG, "onResults");
                    ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    startVoiceRead();
                    messageText += text.get(0);
                    messageText += " ";
                   // processingText(text.get(0));
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }
            });
        }
        return mSpeechRecognizer;
    }

    private void processingText(String text) {
        text = isReadyToSend(text);
        if(!changeField(text)) {
            if (fieldEnum == FieldEnum.contact) {
                setContactText(text);
            } else {
                messageText = Algorithm.interpunction(text);
                messageField.setText(messageText);
                messageField.setSelection(messageField.length());
            }
        }
    }

    private String getContactFromBook(String text) {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        String number = null;
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (text.equalsIgnoreCase(name))
                number = phoneNumber;
        }
        phones.close();
        return number;
    }

    private void setContactText(String text) {
        correctContact = false;
        if (text.length() < 1)
            return;
        try {
            contactText = text.replace(" ", "");
            Long.valueOf(contactText);
            contactField.setText(contactText);
            correctContact = true;
        } catch (Exception e) {
            contactText = getContactFromBook(text);
            if (contactText == null)
                contactField.setText("Nie znaleziono: " + text);
            else {
                contactText = contactText.replace("-", "");
                contactField.setText(contactText);
                correctContact = true;
            }
        }
    }

    private String isReadyToSend (String text) {
        if (sendMessage) {
            if(text.equalsIgnoreCase("tak")) {
                Toast.makeText(getApplicationContext(), "Wysyłanie wiadomości...", Toast.LENGTH_LONG).show();
                stopVoiceRead();
            } else {
                sendMessage = false;
            }
        } else {
            if (text.equalsIgnoreCase("wyślij SMS")) {
                if(correctContact) {
                    Toast.makeText(getApplicationContext(), "Czy na pewno chcesz wysłać SMS?", Toast.LENGTH_LONG).show();
                    sendMessage = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Nieprawidłowy kontakt", Toast.LENGTH_LONG).show();
                    sendMessage = false;
                }
            } else {
                sendMessage = false;
                return text;
            }
        }
        return "";
    }

    private boolean changeField(String text) {
        boolean change = false;
        String[] parts = text.split(" ");
        if (parts.length > 2)
            return change;

        for (int i=0; i<parts.length; i++) {
            if(parts[i].equalsIgnoreCase("kontakt") && fieldEnum == FieldEnum.message) {
                fieldEnum = FieldEnum.contact;
                contactTextView.setTypeface(Typeface.DEFAULT_BOLD);
                messageTextView.setTypeface(Typeface.DEFAULT);
                contactField.requestFocus();
                contactField.setSelection(contactField.length());
                change = true;
            }
            if(parts[i].equalsIgnoreCase("wiadomość") && fieldEnum == FieldEnum.contact) {
                fieldEnum = FieldEnum.message;
                contactTextView.setTypeface(Typeface.DEFAULT);
                messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
                messageField.requestFocus();
                messageField.setSelection(messageField.length());
                change = true;
            }
        }
        return change;
    }

}
