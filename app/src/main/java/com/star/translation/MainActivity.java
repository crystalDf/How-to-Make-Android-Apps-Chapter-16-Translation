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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


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

                    new SaveTheFeed().execute();

                } else {

                    Toast.makeText(MainActivity.this, "Enter Words to Translate",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private class SaveTheFeed extends AsyncTask<Void, Void, Void> {

        String jsonString = "";

        String result = "";

        @Override
        protected Void doInBackground(Void... params) {

            String wordsToTranslate = mEditText.getText().toString();

            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            InputStream inputStream = null;

            try {
                URL url = new URL(getString(R.string.server_url) + wordsToTranslate);

                inputStream = url.openStream();

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));

                StringBuffer stringBuffer = new StringBuffer();

                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                jsonString = stringBuffer.toString();

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

}
