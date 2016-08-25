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
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    public static String readyMessageText = "";
    public static String originalReceivedMessage = "";
    public static String changedReceivedMessage = "";
    public static String[] partsMessage;

    public String contactText = "";
    private boolean correctContact = false;

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
                        Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_LONG).show();
                } else {
                    stopVoiceRead();
                    Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_LONG).show();
                }
            }
        });

         clearButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 contactText = "";
                 readyMessageText = "";
                 messageField.setText(readyMessageText);
                 contactField.setText(contactText);
                 stopVoiceRead();
                 startVoiceRead();
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
                        temp = text.get(0);
                        if (!originalReceivedMessage.equals(temp)) {
                            if (fieldEnum == FieldEnum.message) {
                                originalReceivedMessage = temp;
                                if (processingText(originalReceivedMessage)) {
                                    messageField.setText(readyMessageText + changedReceivedMessage);
                                    messageField.setSelection(messageField.length());
                                }
                            } else {
                                changedReceivedMessage = temp;
                                setContactText(changedReceivedMessage);
                            }
                        }
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    Log.d(TAG, "onResults");
                    ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    startVoiceRead();
                    if (text.size() > 0) {
                        if (fieldEnum == FieldEnum.message) {
                            originalReceivedMessage = text.get(0);
                            processingText(originalReceivedMessage);
                            readyMessageText += changedReceivedMessage;
                            messageField.setText(readyMessageText);
                            messageField.setSelection(messageField.length());
                        } else {
                            changedReceivedMessage = text.get(0);
                            setContactText(changedReceivedMessage);
                        }
                    }
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }
            });
        }
        return mSpeechRecognizer;
    }

    private boolean processingText(String text) {
        String[] parts = text.split(" ");
        if (!changeField(parts)) {
            if (!sendSMS(parts)) {
                changedReceivedMessage = Algorithm.interp(parts);
                return true;
            }
        }
        return false;
    }

    private boolean sendSMS(String[] parts) {
        for (int i=0; i<parts.length; i++) {
            if(parts[i].equalsIgnoreCase("edytorze")) {
                if (i + 2 < parts.length) {
                    if (parts[i+1].equalsIgnoreCase("wyślij") && parts[i+2].equalsIgnoreCase("sms")) {
                        // TODO
                        changedReceivedMessage = changedReceivedMessage.replace(" edytorze", "");
                        changedReceivedMessage = changedReceivedMessage.replace(" Edytorze", "");
                        changedReceivedMessage = changedReceivedMessage.replace(" wyślij", "");
                        changedReceivedMessage = changedReceivedMessage.replace(" Wyślij", "");
                        readyMessageText += changedReceivedMessage;
                        messageField.setText(readyMessageText);

                       // Toast.makeText(getApplicationContext(), "Wysyłanie wiadomości...", Toast.LENGTH_LONG).show();
                        stopVoiceRead();
                        try {
                            SmsManager smsManager=SmsManager.getDefault();
                            smsManager.sendTextMessage(contactText,null,readyMessageText,null,null);
                            Toast.makeText(getApplicationContext(),"SMS Sent!", Toast.LENGTH_LONG).show();
                            //    Log.d("myapp","hello..");
                        }
                        catch (  Exception e) {
                            Toast.makeText(getApplicationContext(),"SMS faild, please try again later!",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getContactFromBook(String text) {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String number = null;
        while (phones.moveToNext())
        {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (text.equalsIgnoreCase(name))
                number = phoneNumber;
        }
        phones.close();
        return number;
    }

    private void setContactText(String text) {
        String[] parts = text.split(" ");
        if (!changeFieldContact(parts)) {
            if(!sendSMSContact(parts)) {
                if(!clearContact(parts)) {
                    correctContact = false;
                    if (text.length() < 1)
                        return;
                    try {
                       // contactText = text;
                        Long.valueOf(contactText);
                        contactField.setText(contactText);
                        correctContact = true;
                    } catch (Exception e) {
                        contactText = getContactFromBook(contactText);
                        if (contactText == null)
                            contactField.setText("Nie znaleziono: " + changedReceivedMessage);
                        else {
                            contactText = contactText.replace("-", "");
                            contactField.setText(contactText);
                            correctContact = true;
                        }
                    }
                }
            }
        }
    }

    private boolean sendSMSContact(String[] parts) {
        for (int i=0; i<parts.length; i++) {
            if(parts[i].equalsIgnoreCase("edytorze")) {
                if (i == parts.length-1)
                    return true;

                if (i + 1 < parts.length) {
                    if (parts[i+1].equalsIgnoreCase("wyślij")) {
                        if (i+1 == parts.length-1)
                            return true;
                    }
                }
                if (i + 2 < parts.length) {
                    if (parts[i+1].equalsIgnoreCase("wyślij") && parts[i+2].equalsIgnoreCase("sms")) {
                        // TODO
                        Toast.makeText(getApplicationContext(), "Wysyłanie wiadomości...", Toast.LENGTH_LONG).show();
                        stopVoiceRead();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean changeFieldContact(String[] parts) {
        for (int i=0; i<parts.length; i++) {
            if(parts[i].equalsIgnoreCase("edytorze")) {
                if (i == parts.length-1)
                    return true;

                if (i + 1 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wiadomość") && fieldEnum == FieldEnum.contact) {
                        stopVoiceRead();
                        startVoiceRead();
                        fieldEnum = FieldEnum.message;
                        contactTextView.setTypeface(Typeface.DEFAULT);
                        messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        messageField.requestFocus();
                        messageField.setSelection(messageField.length());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean changeField(String[] parts) {
        for (int i=0; i<parts.length; i++) {
            if(parts[i].equalsIgnoreCase("edytorze")) {
                if (i + 1 < parts.length) {
                    if (parts[i+1].equalsIgnoreCase("kontakt") && fieldEnum == FieldEnum.message) {
                        fieldEnum = FieldEnum.contact;

                        changedReceivedMessage = changedReceivedMessage.replace(" edytorze", "");
                        changedReceivedMessage = changedReceivedMessage.replace(" Edytorze", "");
                        readyMessageText += changedReceivedMessage;
                        messageField.setText(readyMessageText);
                        messageField.setSelection(messageField.length());

                        stopVoiceRead();
                        startVoiceRead();
                        contactTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        messageTextView.setTypeface(Typeface.DEFAULT);
                        contactField.requestFocus();
                        contactField.setSelection(contactField.length());
                        return true;
                    }
                }
            }
            if(parts[i].equalsIgnoreCase("edytorze")) {
                if (i + 1 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wiadomość") && fieldEnum == FieldEnum.contact) {
                        stopVoiceRead();
                        startVoiceRead();
                        fieldEnum = FieldEnum.message;
                        contactTextView.setTypeface(Typeface.DEFAULT);
                        messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        messageField.requestFocus();
                        messageField.setSelection(messageField.length());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean clearContact(String[] parts) {
        for (int i = parts.length-1; i >= 0; i--) {
            if(parts[i].equalsIgnoreCase("edytorze")) {
                if (i == parts.length-1)
                    return true;

                if (i + 1 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wyczyść")) {
                        changedReceivedMessage = "";
                        if (i+1 == parts.length-1) {
                            contactField.setText(changedReceivedMessage);
                            return true;
                        } else {
                            for (int j = 0; j < parts.length; j++) {
                                if (j < i + 2)
                                    parts[j] = "";
                                changedReceivedMessage += parts[j];
                                if (j >= i+2 && j != parts.length-1)
                                    changedReceivedMessage += " ";
                            }
                            contactText = changedReceivedMessage;
                            return false;
                        }
                    }
                }
            }
        }
        contactText = changedReceivedMessage;
        return false;
    }

}
