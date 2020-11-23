package my.cs2115hw3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private Button startBtn;
    private TextInputLayout inputHints;
    private TextInputEditText inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBtn = findViewById(R.id.startbtn);
        inputText = findViewById(R.id.inputText);
        inputHints = findViewById(R.id.inputHints);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int input = 0;
                if (!inputText.getText().toString().equals("")){
                    input = Integer.parseInt(inputText.getText().toString());
                }
                if (input >= 2 && input <= 5){
                    inputHints.setError("");
                    intent = new Intent(getApplicationContext(), Playground.class);
                    intent.putExtra("colorNum", input);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Color Number must between 2 - 5", Toast.LENGTH_LONG).show();
                    inputHints.setError("Color Number must between 2 - 5");
                }
            }
        });

    }


}