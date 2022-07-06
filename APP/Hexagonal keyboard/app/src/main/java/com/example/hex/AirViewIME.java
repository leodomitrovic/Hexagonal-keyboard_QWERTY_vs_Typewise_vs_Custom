package com.example.hex;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


// Custom IME (Input Method)
// Tipkovnica kao "regularni servis" koji ce se vizualizirati unutar svake aplikacije i
// odgovarajuceg slucaja koristenja kod kojeg se zahtijeva unos teksta.
public class AirViewIME extends InputMethodService
                        /*implements KeyboardView.OnKeyboardActionListener*/ {

    Context context;
    LayoutInflater inflater;
    com.example.hex.KeyboardLayout layout;
    boolean HAND = true;                   // character case (lower/upper => true/false)
    Integer KEYBOARD_LAYOUT = 0;             // velicina tipkovnice (u odnosu na zaslon) - deafult


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        inflater = this.getLayoutInflater();

        // Sprjecavanje zatamnjivanja ekrana (dimming) nakon odredjenog perioda neaktivnosti:
        if (getWindow().getWindow() != null)
        {
            getWindow().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Sva aplikacijska logika tipkovnice smjestena je u razred koji opisuje/definira
        // ponasanje te tipkovnice (KeyboardLayout.java).

        // Instanciranje objekta iz ovoga razreda mozemo napraviti u metodi onWindowShown:
        // @Android API:
        // onWindowShown -- Called when the input method window has been shown to the user,
        // after previously not being visible.
        // [Napomena: pogledati i zivotni ciklus IME-a za vazne metode u tom ciklusu]:
        // https://developer.android.com/guide/topics/text/creating-input-method.html
    }


    @Override
    public void onWindowShown() {
        enableKeyboard();
    }


    // Instanciranje layout-a tipkovnice sa svim potrebnim postavkama
    // i konkretno apliciranje tih postavki
    private void enableKeyboard(){
        // Ucitavanje aktualnih postavki tipkovnice iz Shared Preferences:
        loadSettingsFromSP();

        // Instranciranje layouta tipkovnice:
        layout = new com.example.hex.KeyboardLayout(inflater, context,
                this.getCurrentInputConnection(),
                this.getImeAction(this.getCurrentInputEditorInfo().imeOptions),
                KEYBOARD_LAYOUT, HAND);

        // Apliciranje custom tipkovnice:
        setInputView(layout);
        updateInputViewShown(); // <- Re-evaluate whether the soft input area should currently be shown
    }


    // Ucitavanje postavki tipkovnice iz SharedPreferences:
    private void loadSettingsFromSP(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        HAND = sp.getBoolean("hand", true);
        Set<String> set = new HashSet<>();
        Collections.addAll(set, getResources().getStringArray(R.array.entry_values));
        KEYBOARD_LAYOUT = Integer.parseInt(sp.getString("my_keyboard_layout", "0"));
    }


    // Dohvat IME akcije za doticni editor na kojeg se unosna metoda trenutno odnosi.
    // Korisno za implementaciju RETURN (ENTER) tipke koja moze imati razlicite ucinke
    // za razlicite editore (ovisno o vrsti aplikacije).
    // Primjerice, u Viberu bi to mogao biti Send, u Google pretrazivacu "Search", za neki
    // obrazac s vise EditText elemenata - "Next", itd.
    private int getImeAction(int imeAction) {
        int action;
        switch (imeAction & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_DONE:
                action = EditorInfo.IME_ACTION_DONE;
                break;
            case EditorInfo.IME_ACTION_GO:
                action = EditorInfo.IME_ACTION_GO;
                break;
            case EditorInfo.IME_ACTION_NEXT:
                action = EditorInfo.IME_ACTION_NEXT;
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                action = EditorInfo.IME_ACTION_SEARCH;
                break;
            case EditorInfo.IME_ACTION_SEND:
                action = EditorInfo.IME_ACTION_SEND;
                break;
            default:
                action = EditorInfo.IME_ACTION_UNSPECIFIED;
                break;
        }
        return action;
    }


    // @Android API:
    // Called to inform the input method that text input has started in an editor.
    // You should use this callback to initialize the state of your input to match the
    // state of the editor given to it.
    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting){
        // Azuriranje input connection objekta, tako da se input stream
        // moze aplicirati prilikom (re)starta bilo kojeg editora u bilo kojoj aplikaciji.
        // Primjerice, bez ove metode bi u Viber-u mogli poslati neku poruku, no nakon
        // slanja, novi tekst se ne bi aplicirao u Viber editoru...
        if (layout != null) {
            layout.setInputConnection(this.getCurrentInputConnection());
            layout.setIMEaction(this.getImeAction(attribute.imeOptions));
        }
    }


    // Promjena konfiguracije (tipicno: promjena orijentacije uredjaja)
    // "re-inicijalizacija" tipkovnice:
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableKeyboard();
    }


    @Override
    public void onFinishInputView(boolean finishingInput) {
        // Oslobadjanje resursa, npr. ako se koriste neki listeneri, timeri i slicno...
    }

}
