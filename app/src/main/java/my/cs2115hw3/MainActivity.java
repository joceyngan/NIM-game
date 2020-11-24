package my.cs2115hw3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private Button startBtn;
    private TextInputLayout inputHints;
    private TextInputEditText inputText;
    private ToggleButton computerTB, demoTB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBtn = findViewById(R.id.startbtn);
        inputText = findViewById(R.id.inputText);
        inputHints = findViewById(R.id.inputHints);
        computerTB = findViewById(R.id.computerTB);
        demoTB = findViewById(R.id.demoTB);



        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isComputer = computerTB.isChecked();
                boolean isDemo = demoTB.isChecked();
                int input = 0;
                if (!inputText.getText().toString().equals("")){
                    input = Integer.parseInt(inputText.getText().toString());
                }
                if (input >= 2 && input <= 5){
                    inputHints.setError("");
                    intent = new Intent(getApplicationContext(), Playground.class);

                    intent.putExtra("colorNum", input);
                    intent.putExtra("isComputer", isComputer);
                    intent.putExtra("isDemo", isDemo);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Color Number must between 2 - 5", Toast.LENGTH_LONG).show();
                    inputHints.setError("Color Number must between 2 - 5");
                }
            }
        });

    }


}