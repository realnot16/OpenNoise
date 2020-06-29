package com.example.opennoise;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText email;
    private EditText password;
    private EditText confPassword;
    private EditText nome;
    private EditText cognome;
    private EditText indirizzo;
    private Switch staff;
    private Spinner tipo;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);
        initUI();
    }

    //Inizializzazione
    private void initUI() {
        Log.i(TAG, "Creata SignUpActivity");
        staff = findViewById(R.id.signUp_staff_switch_id);
        tipo = findViewById(R.id.signUp_staff_spinner_id);
        staff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    tipo.setVisibility(View.VISIBLE);
                else
                    tipo.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI(currentUser);
    }


    public void goToLogin(View view){
        Log.i(TAG, "Account già registrato");
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //CREDO NUOVO ACCOUNT SUL DB
    public void createAccount(View view){

        Log.i(TAG, "Hai cliccato su Registrati!");
        email = findViewById(R.id.signUp_email_id);
        password = findViewById(R.id.signUp_password_id);
        confPassword = findViewById(R.id.signUp_confpwd_id);
        nome = findViewById(R.id.signUp_name_id);
        cognome = findViewById(R.id.signUp_lastname_id);
        indirizzo = findViewById(R.id.signUp_indirizzo_id);

        boolean validRegistration=false;
        validRegistration = validateUser(email, password, confPassword, nome, cognome, indirizzo);
        if (staff.isChecked()&&validRegistration) validRegistration= validateStaff(tipo);

        if(validRegistration){
            user = new User(email.getText().toString(),password.getText().toString(),nome.getText().toString()
                            ,cognome.getText().toString(),indirizzo.getText().toString(),staff.isChecked());
            if(staff.isChecked()) user.setType(tipo.getSelectedItem().toString());
            new UploadUser().execute(user);
        }

    }

    //VALIDAZIONE CAMPI
    private boolean validateUser(EditText email, EditText password, EditText confP, EditText nome, EditText cognome, EditText indirizzo) {
        boolean valid = true;
        Pattern emailPattern = Pattern
                .compile("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher emailMatcher = emailPattern.matcher(email.getText().toString());

        Pattern passwordPattern = Pattern
                .compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}");
        Matcher passMatcher = passwordPattern.matcher(password.getText().toString());

        if (TextUtils.isEmpty(nome.getText().toString())) {
            nome.setError("Campo obbligatorio!");
            valid = false;
        }
        if(TextUtils.isEmpty(cognome.getText().toString())) {
            cognome.setError("Campo obbligatorio!");
            valid = false;
        }
        if (TextUtils.isEmpty(indirizzo.getText().toString())){
            indirizzo.setError("Campo obbligatorio!");
            valid = false;
        }
        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Campo obbligatorio!");
            valid = false;
        }
        if(TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Campo obbligatorio!");
            valid = false;
        }
        if (TextUtils.isEmpty(confP.getText().toString())){
            confP.setError("Campo obbligatorio!");
            valid = false;
        }

        //Se i campi obbligatori sono stati compilati:
        if(valid==true) {
            if (!emailMatcher.matches()) {
                valid = false;
                email.setError("Ricontrolla la mail inserita");
            }
            if (!passMatcher.matches()) {
                valid = false;
                Toast.makeText(this, "La password deve contenere almeno 1 maiuscola, 1 minuscola e un numero, e deve essere di almeno 8 caratteri.", Toast.LENGTH_LONG).show();
                password.getText().clear();
                confP.getText().clear();
            }
            if (password.getText().toString().compareTo(confP.getText().toString()) != 0) {
                confP.setError("Le password non coincidono!");
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateStaff(Spinner tipo){
        boolean valid=true;
        if (tipo.getSelectedItem().toString().compareTo("Seleziona il tipo...")==0){
            Toast.makeText(SignUpActivity.this, "Selezionare un campo staff", Toast.LENGTH_LONG).show();
            valid = false;
        }
        return valid;
    }

    //TASK PER CARICARE NUOVO UTENTE SUL DB
    private class UploadUser extends AsyncTask<User, Void, Boolean> {

        @Override
        protected Boolean doInBackground(User... users) {

            String url = "https://quietspace.altervista.org/SignUp.php";
            String params = null;
            String special;
            User p = users[0];
            /*
            if(p.isStaff()){
                special="&tipo="+p.getType();
            }
            else{
                if(p.isIt())
                    special="&tecnico="+1;
                else
                    special="&tecnico="+0;
            }*/


            //Encoding parametri:

            try {
                params = "email=" + URLEncoder.encode(p.getEmail(), "UTF-8")
                        + "&password=" + URLEncoder.encode(p.getPassword(), "UTF-8")
                        + "&nome=" + URLEncoder.encode(p.getNome(), "UTF-8")
                        + "&cognome=" + URLEncoder.encode(p.getCognome(), "UTF-8")
                        + "&indirizzo=" + URLEncoder.encode(p.getIndirizzo(), "UTF-8");
                if(p.isStaff())
                    params += "&tipo="+URLEncoder.encode(p.getType(),"UTF-8");


                Log.i(TAG, "Contatto il DB");
                JSONArray jsonArray=ServerTask.askToServer(params,url);
                //gestisci JsonArray
                JSONObject jsonObjectControl=jsonArray.getJSONObject(0);
                String control=jsonObjectControl.getString("control");
                Log.i(TAG, control);
                if (control.equals("OK")){   // non esegue l'if
                    //UTENTE REGISTRATO CORRETTAMENTE
                    Log.i(TAG, "Utente registrato correttamente.");
                    return true;
                }

            }
            catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if(b){
                Log.i(TAG, "Account Creato, redirect a LoginActivity");
                Toast.makeText(SignUpActivity.this, "Account creato correttamente", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Log.i(TAG, "Account NON Creato, Problema durante la registrazione");
                Toast.makeText(SignUpActivity.this, "ATTENZIONE: Problema durante la registrazione, riprovare.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }


}
