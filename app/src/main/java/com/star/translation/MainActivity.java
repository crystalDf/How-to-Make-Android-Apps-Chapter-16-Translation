package com.star.translation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity {

    private EditText mEditText;
    private Button mSpeakButton;
    private Button mTranslateButton;
    private Button mReadButton;
    private TextView mTranslationTextView;

    private Locale mCurrentSpokenLang = Locale.US;

    private Locale mLocaleSpanish = new Locale("es", "MX");
    private Locale mLocaleRussian = new Locale("ru", "RU");
    private Locale mLocalePortuguese = new Locale("pt", "BR");
    private Locale mLocaleDutch = new Locale("nl", "NL");

    private Locale[] mLanguages = {mLocaleDutch, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN,
            mLocalePortuguese, mLocaleRussian, mLocaleSpanish};

    private TextToSpeech mTextToSpeech;

    private Spinner mLanguageSpinner;

    private int mSpinnerIndex = 0;

    private String[] mArrayOfTranslation;

    public static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edit_text);

        mSpeakButton = (Button) findViewById(R.id.speak_button);
        mTranslateButton = (Button) findViewById(R.id.translate_button);
        mTranslationTextView = (TextView) findViewById(R.id.translation_text_view);

        mSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());

                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_input_phrase));

                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, R.string.stt_not_supported_message,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mTranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String wordsToTranslate = mEditText.getText().toString();

                if (!TextUtils.isEmpty(wordsToTranslate)) {

                    Toast.makeText(MainActivity.this, "Getting Translations",
                            Toast.LENGTH_LONG).show();

//                    new GetJsonData().execute();
                    new GetXmlData().execute();

                } else {

                    Toast.makeText(MainActivity.this, "Enter Words to Translate",
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        mLanguageSpinner = (Spinner) findViewById(R.id.lang_spinner);

        mLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentSpokenLang = mLanguages[position];

                mSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTextToSpeech.setLanguage(mCurrentSpokenLang);

                    if ((result == TextToSpeech.LANG_MISSING_DATA) ||
                            (result == TextToSpeech.LANG_NOT_SUPPORTED)) {
                        Toast.makeText(MainActivity.this, "Language Not Supported",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Text To Speech Failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mReadButton = (Button) findViewById(R.id.read_button);

        mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextToSpeech.setLanguage(mCurrentSpokenLang);

                if (mArrayOfTranslation.length >= 9) {
                    mTextToSpeech.speak(mArrayOfTranslation[mSpinnerIndex + 4],
                            TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    Toast.makeText(MainActivity.this, "Translate Text First",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }

        super.onDestroy();
    }

    private class GetJsonData extends AsyncTask<Void, Void, Void> {

        String jsonString = "";

        String result = "";

        @Override
        protected Void doInBackground(Void... params) {

            String wordsToTranslate = mEditText.getText().toString();

            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            InputStream inputStream = null;

            try {
                URL url = new URL(getString(R.string.server_json_url) + wordsToTranslate);

                inputStream = url.openStream();

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();

                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                jsonString = stringBuilder.toString();

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONArray jsonArray = jsonObject.getJSONArray(getString(R.string.json_array_name));

                outputTranslations(jsonArray);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTranslationTextView.setText(result);
        }

        private void outputTranslations(JSONArray jsonArray) {

            String[] languages = getResources().getStringArray(R.array.languages);

            try {
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    result += (languages[i] + " : " + jsonObject.getString(languages[i]) + "\n");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetXmlData extends AsyncTask<Void, Void, Void> {

        String result = "";

        @Override
        protected Void doInBackground(Void... params) {

            String wordsToTranslate = mEditText.getText().toString();

            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            InputStream inputStream = null;

            try {
                URL url = new URL(getString(R.string.server_xml_url) + wordsToTranslate);

                inputStream = url.openStream();

                DocumentBuilderFactory documentBuilderFactory =
                        DocumentBuilderFactory.newInstance();

                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                Document document = documentBuilder.parse(inputStream);

                Element element = document.getDocumentElement();

                outputTranslations(element);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            mTranslationTextView.setMovementMethod(new ScrollingMovementMethod());

            String stringOfTranslations = result.replaceAll("\\w+\\s:", "#");

            mArrayOfTranslation = stringOfTranslations.split("#");

            mTranslationTextView.setText(result);
        }

        private void outputTranslations(Element element) {

            String[] languages = getResources().getStringArray(R.array.languages);
            Element[] elements = new Element[languages.length];

            for (int i = 0; i < languages.length; i++) {
                elements[i] = (Element) element.getElementsByTagName(languages[i]).item(0);

                result += (languages[i] + " : " + elements[i].getFirstChild().getNodeValue() + "\n");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == REQUEST_CODE) && (data != null) && (resultCode == RESULT_OK)) {
            List<String> spokenText = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
            );

            EditText wordsEntered = (EditText) findViewById(R.id.edit_text);

            wordsEntered.setText(spokenText.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
