package textrecognition.blog.com.textrecognition;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileCreatorActivity extends AppCompatActivity {

    private TextView nameInput;
    private TextView companyInput;
    private TextView telephoneInput;
    private TextView emailInput,input_website;

    Map<String, Integer> phoneNumberCandidates = new HashMap<String, Integer>();
    Map<String, Integer> emailCandidates = new HashMap<String, Integer>();
    Map<String, Integer> webSiteCandidates = new HashMap<>();
    Map<String, Integer> userNameCandidates = new HashMap<>();
    List<String> genericCandidates = new ArrayList<String>();
    List<String> nameCandidates = new ArrayList<String>();
    List<String> companyCandidates = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creator);
        nameInput = (TextView) findViewById(R.id.input_name);
        companyInput = (TextView) findViewById(R.id.input_company);
        telephoneInput = (TextView) findViewById(R.id.input_telephone);
        emailInput = (TextView) findViewById(R.id.input_email);
        input_website = (TextView)findViewById(R.id.input_website);

        if (!generateProfile()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.profile_creator_alert_read_fail);
            builder.setNeutralButton(R.string.profile_creator_alert_read_fail_retry,
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(ProfileCreatorActivity.this,
                                    text_Recognition_camera.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            builder.setNegativeButton(R.string.profile_creator_alert_read_fail_manual,
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            builder.setPositiveButton(R.string.profile_creator_alert_read_fail_exit,
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(ProfileCreatorActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            builder.show();
        }

        Button saveButton = (Button) findViewById(R.id.save_button);
        Button rescanButton = (Button) findViewById(R.id.rescan_button);
        Button exitButton = (Button) findViewById(R.id.exit_button);

        saveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
               Intent intent = new Intent(ProfileCreatorActivity.this,MainActivity.class);
               startActivity(intent);
            }
        });
        rescanButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                confirmRescan();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                confirmExit();
            }
        });

    }

    private void confirmRescan(){
        dialogConfirm(R.string.profile_creator_confirm_rescan,
                R.string.profile_creator_button_rescan,
                text_Recognition_camera.class);
    }

    private void confirmExit(){
        dialogConfirm(R.string.profile_creator_confirm_exit,
                R.string.profile_creator_button_exit,
                MainActivity.class);
    }

    private void dialogConfirm(int dialogMessage,
                               int confirmMessage,
                               final Class newActivityClass){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_confirmation);
        builder.setMessage(dialogMessage);
        builder.setNegativeButton(R.string.dialog_cancel,
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        builder.setPositiveButton(confirmMessage,
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(ProfileCreatorActivity.this,
                                newActivityClass);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.show();
    }


    private boolean generateProfile() {
        ArrayList<String> profileData;
        try{
            profileData = getIntent().getStringArrayListExtra(text_Recognition_camera.PROFILE_DATA_KEY);
        } catch(Exception e) {
            Log.w(ProfileCreatorActivity.class.getName(), Log.getStackTraceString(e));
            return false;
        }

        for (String snapshot : profileData){
            for (String text : snapshot.split("\n")){
                int selected = 0;
                selected = selectPhoneNumber(text, phoneNumberCandidates)
                        + selectEmail(text, emailCandidates)
                        + selectUserName(text, userNameCandidates)
                        + selectWebsite(text, webSiteCandidates);
                if (selected == 0) {
                    selectRest(text, genericCandidates);
                }
            }
        }
        boolean generateProfile = false;
        String phoneNumber = getBestCandidate(phoneNumberCandidates);
        if (StringUtils.isNotBlank(phoneNumber)){
            generateProfile = true;
            telephoneInput.setText(phoneNumber);
        }

        String email = getBestCandidate(emailCandidates);

        if (StringUtils.isNotBlank(email)){
            generateProfile = true;
            emailInput.setText(email);
            String namePart = email.substring(0, email.indexOf("@"));
            String companyPart = email.substring(email.indexOf("@")+1, email.length());
            companyPart = companyPart.substring(0, companyPart.indexOf("."));

            StringBuilder nameBuilder = new StringBuilder();
            int j = 0;
            for (String str : namePart.split("\\.")){
                j++;
                nameBuilder.append(str.substring(0, 1).toUpperCase());
                if (str.length() > 1){
                    nameBuilder.append(str.substring(1));
                }
                nameBuilder.append(" ");
            }
            if (j > 0) {
                nameCandidates.add(nameBuilder.toString().trim());
            }

            if (companyPart.length() > 1
                    && !companyPart.equals("googlemail")
                    && !companyPart.equals("gmail")
                    && !companyPart.equals("hotmail")
                    && !companyPart.equals("live")){
                companyCandidates.add(companyPart.substring(0, 1).toUpperCase()+companyPart.substring(1));
                companyCandidates.add(companyPart.toUpperCase());
                companyCandidates.add(companyPart);
            }

        }

        nameCandidates.addAll(genericCandidates);
        companyCandidates.addAll(genericCandidates);

        if (!nameCandidates.isEmpty()){
//            nameInput.setText(nameCandidates.get(0));
            generateProfile = true;
        }
        int i = 0;
        if (!companyCandidates.isEmpty()){
            if (companyCandidates.get(0).equals(nameCandidates.get(0)) && companyCandidates.size() != 1){
                i++;
            }
            companyInput.setText(companyCandidates.get(i));
        }

        String userName = getBestCandidate(userNameCandidates);
        if (StringUtils.isNotBlank(userName)){
           generateProfile = true;
            nameInput.setText(userName);
        }

        String website = getBestCandidate(webSiteCandidates);
        if (StringUtils.isNotBlank(website)){
            generateProfile = true;
            input_website.setText(website);
        }else {
            generateProfile = false;
        }

        genericCandidates.addAll(phoneNumberCandidates.keySet());
        genericCandidates.addAll(emailCandidates.keySet());
        genericCandidates.addAll(webSiteCandidates.keySet());
        genericCandidates.addAll(userNameCandidates.keySet());
        return generateProfile;
    }


    private void selectRest(String text, List<String> genericCandidates) {
        List<String> toFilter = new ArrayList<String>();
        boolean filter = false;
        for (String candidate : genericCandidates){
            if (candidate.contains(text)){
                filter = true;
                break;
            }
            if (text.contains(candidate)){
                toFilter.add(candidate);
            }
        }
        if (!filter){
            genericCandidates.add(text);
        }
        genericCandidates.removeAll(toFilter);
    }

    private int selectPhoneNumber(String text, Map<String, Integer> phoneNumberCandidates) {
        //At least 6 numbers, allow other characters
        String trimmed = text.toLowerCase().replaceAll("tel:","").replaceAll("mob:","").trim();
        if (phoneNumberCandidates.containsKey(trimmed)) {
            phoneNumberCandidates.put(trimmed, phoneNumberCandidates.get(trimmed) + 1);
        } else {
            int numCount = 0;

            for (char c : trimmed.toCharArray()) {
                if (Character.isDigit(c)) {
                    numCount++;
                }
                if (numCount == 6) {
                    phoneNumberCandidates.put(trimmed, 1);
                    return 1;
                }
            }
        }
        return 0;
    }
    private int selectEmail(String text, Map<String, Integer> emailCandidates) {
        int atPos = text.indexOf("@");
        int dotPos = text.lastIndexOf(".");
        //Very basic check to see if a text COULD BE an email address
        if (atPos != -1 && dotPos > atPos){
            String trimmed = text.trim();
            if (emailCandidates.containsKey(trimmed)){
                emailCandidates.put(trimmed, emailCandidates.get(trimmed)+1);
            } else {
                emailCandidates.put(trimmed, 1);
            }
            return 1;
        }
        return 0;
    }

    private int selectUserName(String text, Map<String, Integer> userNameCandidates) {

        int atPos = text.indexOf("[A-Z]");
        int dotPos = text.lastIndexOf(" [A-Z]");

//        if (atPos != -1 && dotPos > atPos) {

            String trimmed = text.trim();
            Log.e("selectUserName", "selectUserName: " + text );
            if (userNameCandidates.containsKey(trimmed)) {
                userNameCandidates.put(text, userNameCandidates.get(trimmed) + 1);
            } else {
                userNameCandidates.put(trimmed, 1);
            }
            return 1;
//        }
//        return 0;
    }

    private int selectWebsite(String text, Map<String, Integer> webSiteCandidates) {
        // WEB PAGE EXTRACTION BEGINS
        Pattern ptr1 = Pattern.compile("(http:\\/\\/|https:\\/\\/|www.)?[a-z]{5}.?([a-z]+)?(.com|.in|.edu|.ca|.usa)$");
        Matcher webpageMatcher = ptr1.matcher(text);

        while (webpageMatcher.find()) {
            Log.d("TAG", "website: " + webpageMatcher.group() + "\n" + webpageMatcher.start() + "\n" + webpageMatcher.end());
            String trimmed = text.trim();
            if (webSiteCandidates.containsKey(trimmed)) {
                webSiteCandidates.put(webpageMatcher.group(), webSiteCandidates.get(trimmed) + 1);
            } else {
                webSiteCandidates.put(trimmed, 1);
            }
        }
        return 1;
    }


    private String getBestCandidate(Map<String, Integer> candidates){
        int maxValue = 0;
        String bestCandidate ="";
        for (Map.Entry<String, Integer> candidate : candidates.entrySet()){
            if (candidate.getValue() > maxValue){
                maxValue = candidate.getValue();
                bestCandidate = candidate.getKey();
            }
        }
        return bestCandidate;
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(ProfileCreatorActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
