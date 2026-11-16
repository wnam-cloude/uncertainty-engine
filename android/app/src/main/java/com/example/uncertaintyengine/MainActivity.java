package com.example.uncertaintyengine;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView questionText;
    private TextView answerText;
    private Button askButton;
    private TextView historyText;
    private Random random = new Random();
    private LinearLayout answerCard;

    private final String[] uncertainties = {
        "Will the cat ever return?",
        "Is the soup too hot?",
        "Does the socks drawer eat socks?",
        "Will I forget why I entered this room?",
        "Is the universe a simulation?",
        "Does the fridge light turn off?",
        "Will this bug fix itself?",
        "Is today Tuesday or Thursday?",
        "Does WiFi know when I need it?",
        "Will the package arrive?"
    };

    private final String[] responses = {
        "¯\\_(ツ)_/¯",
        "The stars say: maybe.",
        "Ask again when Mercury isn't retrograde.",
        "Unclear. Try shaking the phone.",
        "The void whispers: who knows?",
        "404: Certainty not found.",
        "Consulting the tea leaves...",
        "Even the magic 8-ball is confused.",
        "Probability: unknown.",
        "The universe shrugs."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);
        root.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("UNCERTAINTY ENGINE");
        title.setTextColor(0xFFE8D5B7);
        title.setTextSize(20);
        title.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 32, 0, 8);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("An oracle for life's unanswerable questions");
        subtitle.setTextColor(0xFF8B8B9E);
        subtitle.setTextSize(12);
        subtitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        subtitle.setPadding(0, 0, 0, 48);
        root.addView(subtitle);

        LinearLayout questionCard = new LinearLayout(this);
        questionCard.setOrientation(LinearLayout.VERTICAL);
        questionCard.setBackgroundColor(0xFF252542);
        questionCard.setPadding(24, 24, 24, 24);

        questionText = new TextView(this);
        questionText.setText("Tap to ask the void...");
        questionText.setTextColor(0xFFF5F0E8);
        questionText.setTextSize(16);
        questionText.setGravity(android.view.Gravity.CENTER);
        questionCard.addView(questionText);
        root.addView(questionCard);

        answerCard = new LinearLayout(this);
        answerCard.setOrientation(LinearLayout.VERTICAL);
        answerCard.setBackgroundColor(0xFF2D1B3D);
        answerCard.setPadding(24, 24, 24, 24);
        answerCard.setVisibility(View.GONE);

        answerText = new TextView(this);
        answerText.setTextColor(0xFFD4A5D8);
        answerText.setTextSize(18);
        answerText.setGravity(android.view.Gravity.CENTER);
        answerCard.addView(answerText);
        root.addView(answerCard);

        askButton = new Button(this);
        askButton.setText("ASK THE VOID");
        askButton.setBackgroundColor(0xFF4A2C5E);
        askButton.setTextColor(0xFFF5F0E8);
        askButton.setTextSize(14);
        askButton.setPadding(0, 16, 0, 16);
        askButton.setOnClickListener(this);
        root.addView(askButton);

        TextView historyLabel = new TextView(this);
        historyLabel.setText("HISTORY");
        historyLabel.setTextColor(0xFF8B8B9E);
        historyLabel.setTextSize(12);
        historyLabel.setPadding(16, 32, 0, 8);
        root.addView(historyLabel);

        ScrollView scrollView = new ScrollView(this);
        historyText = new TextView(this);
        historyText.setTextColor(0xFFA09AA8);
        historyText.setTextSize(11);
        historyText.setPadding(16, 0, 16, 24);
        scrollView.addView(historyText);
        root.addView(scrollView);

        setContentView(root);
    }

    @Override
    public void onClick(View v) {
        if (v == askButton) {
            int qIdx = random.nextInt(uncertainties.length);
            int rIdx = random.nextInt(responses.length);

            questionText.setText(uncertainties[qIdx]);
            answerText.setText(responses[rIdx]);
            answerCard.setVisibility(View.VISIBLE);

            String entry = "Q: " + uncertainties[qIdx] + "\nA: " + responses[rIdx] + "\n";
            historyText.setText(entry + historyText.getText());
        }
    }
}
