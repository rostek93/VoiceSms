package com.example.kamil.voicesms;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "VoiceSMS";

    private ImageButton startButton;
    private Button clearButton;
    private Button exitButton;
    private EditText messageField;
    private EditText contactField;
    private TextView messageTextView;
    private TextView contactTextView;
    private FieldEnum fieldEnum;
    private ImageView iconImageView;
    private SpeechRecognizer mSpeechRecognizer = null;

    /** Wiadomość utworzona i zmodyfikowana w poprzednich nagraniach */
    public static String readyMessageText = "";
    /** Oryginalna wiadomość odebrana z serwera */
    public static String originalReceivedMessage = "";
    /** Zmodyfikowana orginalna wiadomość */
    public static String changedReceivedMessage = "";
    /** Zmodyfikowana wiadomość wyświetlana w polu kontakt */
    public static String contactText = "";
    /** Flaga oznaczająca poprawność wprowadzonego kontaktu */
    private boolean correctContact = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bindView();

        messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
        messageField.setSelection(messageField.length());
        messageField.requestFocus();
        iconImageView.setImageResource(R.drawable.redicon);
        fieldEnum = FieldEnum.message;

        startButton.setOnClickListener(new StartButtonListener());
        clearButton.setOnClickListener(new ClearButtonListener());
        exitButton.setOnClickListener(new ExitButtonListener());
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void bindView() {
        startButton = (ImageButton) findViewById(R.id.startButton);
        clearButton = (Button) findViewById(R.id.clearButton);
        exitButton = (Button) findViewById(R.id.exitButton);
        messageField = (EditText) findViewById(R.id.messageField);
        contactField = (EditText) findViewById(R.id.contactField);
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        contactTextView = (TextView) findViewById(R.id.contactTextView);
        iconImageView = (ImageView) findViewById(R.id.imageView);
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return (net != null && net.isAvailable() && net.isConnected());
    }

    private void startVoiceRead() {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        startButton.setImageResource(R.drawable.soundoff);
        getSpeechRecognizer().startListening(speechIntent);
    }

    private void stopVoiceRead() {
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
                    List<String> text = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    receiveResult(text);
                }

                @Override
                public void onResults(Bundle results) {
                    Log.d(TAG, "onResults");
                    /** Get result here, because we want to as fast as possible called startVoiceRead() */
                    List<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    startVoiceRead();
                    receiveResult(text);
                    if (fieldEnum == FieldEnum.message)
                        readyMessageText += changedReceivedMessage;
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }
            });
        }
        return mSpeechRecognizer;
    }

    private void receiveResult(List<String> result) {
        if (result != null) {
            if (result.size() > 0) {
                String text = result.get(0);
                if (!originalReceivedMessage.equals(text) && text.length() > 0) {
                    originalReceivedMessage = text;
                    processingResult();
                }
            }
        }
    }

    private void processingResult() {
        String[] parts = originalReceivedMessage.split(" ");
        if (!changeField(parts)) {
            if (!sendSms(parts)) {
                if (fieldEnum == FieldEnum.message) {
                    processingMessage(parts);
                } else {
                    processingContact(parts);
                }
            }
        }
    }

    private boolean changeField(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("edytorze")) {
                if (i == parts.length - 1) {
                    return true;
                }
                if (i + 1 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wiadomość") && fieldEnum == FieldEnum.contact) {
                        changeFieldToContact();
                        return true;
                    }
                    if (parts[i + 1].equalsIgnoreCase("kontakt") && fieldEnum == FieldEnum.message) {
                        changeFieldToMessage();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void changeFieldToMessage() {
        stopVoiceRead();
        startVoiceRead();
        readyMessageText += changedReceivedMessage;
        fieldEnum = FieldEnum.contact;
        /** To be sure that contatct is correct */
        contactField.setText(contactText);
        contactTextView.setTypeface(Typeface.DEFAULT_BOLD);
        messageTextView.setTypeface(Typeface.DEFAULT);
        contactField.requestFocus();
        contactField.setSelection(contactField.length());
    }

    private void changeFieldToContact() {
        fieldEnum = FieldEnum.message;
        stopVoiceRead();
        startVoiceRead();
        contactTextView.setTypeface(Typeface.DEFAULT);
        messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
        messageField.requestFocus();
        messageField.setSelection(messageField.length());
    }

    private boolean sendSms(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("edytorze")) {
                if (i == parts.length - 1) {
                    return true;
                }
                if (i + 1 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wyślij")) {
                        if (i + 1 == parts.length - 1) {
                            return true;
                        }
                    }
                }
                if (i + 2 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wyślij") && parts[i + 2].equalsIgnoreCase("sms")) {
                        sendSmsReady();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void sendSmsReady() {
        stopVoiceRead();
        if (correctContact) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(contactText, null, readyMessageText, null, null);
                displayToast(Constants.Message.SEND_CORRECT);
            } catch (Exception e) {
                displayToast(Constants.Message.SEND_FAIL);
            }
            try {
                saveSms();
            } catch (Exception e) {
                displayToast(Constants.Message.SAVE_SMS_FAIL);
            }
        } else {
            displayToast(Constants.Message.INCORRECT_CONTACT);
        }
    }

    private void saveSms() {
        ContentValues values = new ContentValues();
        values.put("address", contactText);
        values.put("body", readyMessageText);
        getContentResolver().insert(Uri.parse("content://sms/sent"), values);
    }

    private void processingMessage(String[] parts) {
        changedReceivedMessage = Algorithm.processingText(parts);
        messageField.setText(readyMessageText + changedReceivedMessage);
        messageField.setSelection(messageField.length());
    }

    private void processingContact(String[] parts) {
        changedReceivedMessage = originalReceivedMessage;
        if (!clearContact(parts)) {
            setContactText();
        }
    }

    private String getContactFromBook(String text) {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String number = null;
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (text.equalsIgnoreCase(name)) {
                number = phoneNumber;
            }
        }
        phones.close();
        return number;
    }

    private void setContactText() {
        correctContact = false;
        contactText = changedReceivedMessage;
        try {
            String number = contactText.replaceAll(" ", "");
            Long.valueOf(number);
            contactField.setText(number);
            contactText = number;
            correctContact = true;
        } catch (Exception e) {
            contactText = getContactFromBook(contactText);
            if (contactText == null) {
                contactField.setText(Constants.Message.NOT_FOUND + changedReceivedMessage);
            }
            else {
                contactText = contactText.replace("-", "");
                contactField.setText(contactText);
                correctContact = true;
            }
        }
    }

    private boolean clearContact(String[] parts) {
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].equalsIgnoreCase("edytorze")) {
                if (i == parts.length - 1) {
                    return true;
                }
                if (i + 1 < parts.length) {
                    if (parts[i + 1].equalsIgnoreCase("wyczyść")) {
                        changedReceivedMessage = "";
                        if (i + 1 == parts.length - 1) {
                            contactText = changedReceivedMessage;
                            contactField.setText(contactText);
                            return true;
                        } else {
                            for (int j = 0; j < parts.length; j++) {
                                if (j < i + 2) {
                                    parts[j] = "";
                                }
                                changedReceivedMessage += parts[j];
                                if (j >= i + 2 && j != parts.length - 1) {
                                    changedReceivedMessage += " ";
                                }
                            }
                            contactText = changedReceivedMessage;
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    enum FieldEnum { contact, message }

    class StartButtonListener implements View.OnClickListener {
        @Override
        public void onClick (View v){
            if (mSpeechRecognizer == null) {
                if (isConnected()) {
                    startVoiceRead();
                    displayToast(Constants.Message.START);
                } else {
                    displayToast(Constants.Message.ERROR_NETWORK);
                }
            } else {
                stopVoiceRead();
                readyMessageText += changedReceivedMessage;
                displayToast(Constants.Message.STOP);
            }
        }
    }

    class ClearButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            contactText = "";
            readyMessageText = "";
            messageField.setText(readyMessageText);
            contactField.setText(contactText);
            stopVoiceRead();
        }
    }

    class ExitButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            stopVoiceRead();
            System.exit(0);
        }
    }

}
