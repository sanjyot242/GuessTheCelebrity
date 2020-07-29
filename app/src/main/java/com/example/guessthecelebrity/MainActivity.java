package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    Button option1,option2,option3,option4;

    ArrayList<String> celebrityUrl = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int choosenCeleb,locOfCorrectAns=0;
    String[] answers = new String[4];

    public void checkAnswer(View view) {
        if(view.getTag().toString().equals(Integer.toString(locOfCorrectAns))){
            Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
            newQuestion();
        }else{
            Toast.makeText(this, "Wrong! correct ans is "+celebNames.get(choosenCeleb), Toast.LENGTH_SHORT).show();
        }

    }

    public class downloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    }


    public class downloadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result= "";
            try {
                URL url = new URL(strings[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

               InputStream in = connection.getInputStream();
               InputStreamReader reader = new InputStreamReader(in);

               int data = reader.read();

               StringBuilder inputLine = new StringBuilder() ;
               while (data != -1){
                   char current = (char) data;
                   inputLine.append(current);
                   data= reader.read();

               }
               result=inputLine.toString();
               return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;

            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image=findViewById(R.id.image);
        option1=findViewById(R.id.option0);
        option2=findViewById(R.id.option1);
        option3=findViewById(R.id.option2);
        option4=findViewById(R.id.option3);

        downloadData task = new downloadData();
        String result=null;
        try {
           result = task.execute("https://www.imdb.com/list/ls006992840/").get();
           String[] splitResult = result.split("<div class=\"lister-list\">");

           String[] finalString = splitResult[1].split("<div class=\"row text-center lister-working hidden\">");

            Pattern p = Pattern.compile("alt=\"(.*?)\"");
            Matcher m = p.matcher(finalString[0]);

            while (m.find()){
                celebNames.add(m.group(1));
               // System.out.println(m.group(1));
            }

            p = Pattern.compile("src=\"(.*?)\"");
            m = p.matcher(finalString[0]);

            while (m.find()){
                celebrityUrl.add(m.group(1));
                //System.out.println(m.group(1));
            }
            newQuestion();
           // Log.i("result", "onCreate: "+finalString[0]);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void newQuestion(){
        Random random =new Random();
        choosenCeleb= random.nextInt(celebNames.size());

        downloadImage imageTask = new downloadImage();
        Bitmap celebImage = null;
        try {
            celebImage = imageTask.execute(celebrityUrl.get(choosenCeleb)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        image.setImageBitmap(celebImage);

        locOfCorrectAns= random.nextInt(4);

        for(int i = 0;i<=3;i++){
            if(i==locOfCorrectAns){
                answers[i]=celebNames.get(choosenCeleb);
            }else{
                int locOfWrongAnswer = random.nextInt(celebNames.size());
                while(locOfWrongAnswer == choosenCeleb ){
                    locOfWrongAnswer = random.nextInt(celebNames.size());
                }
                answers[i]=celebNames.get(locOfWrongAnswer);
            }
        }

        option1.setText(answers[0]);
        option2.setText(answers[1]);
        option3.setText(answers[2]);
        option4.setText(answers[3]);
    }
}
