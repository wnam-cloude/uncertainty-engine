package com.example.uncertaintyengine;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView questionText;
    private TextView answerText;
    private Button askButton;
    private TextView historyText;
    private Random random = new Random();

    private final String[] uncertainties = {
        "Will the cat ever return?",
        "Is the soup too hot or just right?",
        "Does the socks drawer eat single socks?",
        "Will I remember why I walked into this room?",
        "Is the universe a simulation?",
        "Does the fridge light actually turn off?",
        "Will this bug fix itself overnight?",
        "Is today a Tuesday or a Thursday?",
        "Does the WiFi know when I need it most?",
        "Will the package arrive before I give up waiting?",
        "Is the noise upstairs a ghost or a neighbor?",
        "Does the plant know I'm talking to it?",
        "Will the coffee kick in before the meeting?",
        "Is that shadow moving or just my imagination?",
        "Does the USB go in on the first try?",
        "Will the alarm actually wake me up?",
        "Is the leftovers container still good?",
        "Does the autoplay know my taste?",
        "Will the traffic light turn green in time?",
        "Is this decision reversible?"
    };

    private final String[] responses = {
        "¯\\_(ツ)_/¯",
        "The stars say: maybe.",
        "Ask again when Mercury isn't retrograde.",
        "Unclear. Try shaking the phone.",
        "The void whispers: 'who knows?'",
        "404: Certainty not found.",
        "Consulting the tea leaves... 🛖",
        "Even the magic 8-ball is confused.",
        "Probability: somewhere between 0 and 1.",
        "The universe shrugs.",
        "Check back in 3-5 business days.",
        "Your guess is as good as the algorithm's.",
        "Error: Reality.exe has stopped responding.",
        "The coins land on their edge today.",
        "Schrodinger's answer: both yes and no.",
        "Loading certainty... ███░░░░░░░ 30%",
        "The dice have no memory.",
        "Fortune cookie says: 'Ask the wind.'",
        "Entropy increases. Certainty decreases.",
        "The answer is a cat in a box."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(0xFF1A1A2E);
        getWindow().setNavigationBarColor(0xFF1A1A2E);

        questionText = findViewById(R.id.questionText);
        answerText = findViewById(R.id.answerText);
        askButton = findViewById(R.id.askButton);
        historyText = findViewById(R.id.historyText);

        askButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askTheVoid();
            }
        });
    }

    private void askTheVoid() {
        int qIdx = random.nextInt(uncertainties.length);
        int rIdx = random.nextInt(responses.length);

        questionText.setText(uncertainties[qIdx]);
        answerText.setText(responses[rIdx]);
        answerText.setVisibility(View.VISIBLE);

        String entry = "Q: " + uncertainties[qIdx] + "\nA: " + responses[rIdx] + "\n";
        String current = historyText.getText().toString();
        historyText.setText(entry + current);
    }
}