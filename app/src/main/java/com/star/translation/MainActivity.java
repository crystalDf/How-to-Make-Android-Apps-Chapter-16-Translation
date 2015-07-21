package com.star.translation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity {

    private EditText mEditText;
    private Button mTranslateButton;
    private TextView mTranslationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edit_text);
        mTranslateButton = (Button) findViewById(R.id.translate);
        mTranslationTextView = (TextView) findViewById(R.id.translation_text_view);

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

}
